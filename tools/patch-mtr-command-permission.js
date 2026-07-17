#!/usr/bin/env node

const fs = require("fs");

const classFile = process.argv[2];

if (!classFile) {
  console.error("Usage: patch-mtr-command-permission.js <org/mtr/MTR.class>");
  process.exit(1);
}

const buffer = fs.readFileSync(classFile);

function readU1(offset) {
  return buffer.readUInt8(offset);
}

function readU2(offset) {
  return buffer.readUInt16BE(offset);
}

function readU4(offset) {
  return buffer.readUInt32BE(offset);
}

let offset = 0;

if (readU4(offset) !== 0xcafebabe) {
  throw new Error("Not a Java class file");
}

offset += 8;
const constantPoolCount = readU2(offset);
offset += 2;

const utf8 = new Map();

for (let index = 1; index < constantPoolCount; index++) {
  const tag = readU1(offset++);

  switch (tag) {
    case 1: {
      const length = readU2(offset);
      offset += 2;
      utf8.set(index, buffer.toString("utf8", offset, offset + length));
      offset += length;
      break;
    }
    case 3:
    case 4:
      offset += 4;
      break;
    case 5:
    case 6:
      offset += 8;
      index++;
      break;
    case 7:
    case 8:
    case 16:
    case 19:
    case 20:
      offset += 2;
      break;
    case 9:
    case 10:
    case 11:
    case 12:
    case 17:
    case 18:
      offset += 4;
      break;
    case 15:
      offset += 3;
      break;
    default:
      throw new Error(`Unsupported constant pool tag ${tag} at ${offset - 1}`);
  }
}

offset += 6;

const interfacesCount = readU2(offset);
offset += 2 + interfacesCount * 2;

function skipMembers(memberCount) {
  for (let i = 0; i < memberCount; i++) {
    offset += 6;
    const attributesCount = readU2(offset);
    offset += 2;

    for (let j = 0; j < attributesCount; j++) {
      offset += 2;
      const attributeLength = readU4(offset);
      offset += 4 + attributeLength;
    }
  }
}

const fieldsCount = readU2(offset);
offset += 2;
skipMembers(fieldsCount);

const methodsCount = readU2(offset);
offset += 2;
let patched = 0;

for (let i = 0; i < methodsCount; i++) {
  offset += 2;
  const name = utf8.get(readU2(offset));
  offset += 2;
  const descriptor = utf8.get(readU2(offset));
  offset += 2;
  const attributesCount = readU2(offset);
  offset += 2;

  for (let j = 0; j < attributesCount; j++) {
    const attributeName = utf8.get(readU2(offset));
    offset += 2;
    const attributeLength = readU4(offset);
    offset += 4;

    if (
      attributeName === "Code" &&
      descriptor === "(Lnet/minecraft/class_2168;)Z" &&
      (name === "lambda$depotOperationFromCommand$21" || name === "lambda$init$0")
    ) {
      const codeLengthOffset = offset + 4;
      const codeLength = readU4(codeLengthOffset);
      const codeOffset = codeLengthOffset + 4;
      const bytecode = buffer.subarray(codeOffset, codeOffset + codeLength);

      const isOriginalPermissionCheck =
        bytecode.length === 6 &&
        bytecode[0] === 0x2a &&
        (bytecode[1] === 0x05 || bytecode[1] === 0x07) &&
        bytecode[2] === 0xb6 &&
        bytecode[5] === 0xac;
      const isPreviousInvalidPatch =
        bytecode.length === 6 &&
        bytecode[0] === 0x04 &&
        bytecode[1] === 0xac &&
        bytecode.subarray(2).every(value => value === 0x00);

      if (!isOriginalPermissionCheck && !isPreviousInvalidPatch) {
        throw new Error(`Unexpected bytecode for ${name}: ${bytecode.toString("hex")}`);
      }

      // Keep every instruction reachable so Java's verifier does not require a
      // new stack map frame for trailing bytecode after an early return.
      buffer[codeOffset] = 0x04;
      buffer[codeOffset + 1] = 0x00;
      buffer[codeOffset + 2] = 0x00;
      buffer[codeOffset + 3] = 0x00;
      buffer[codeOffset + 4] = 0x00;
      buffer[codeOffset + 5] = 0xac;
      patched++;
    }

    offset += attributeLength;
  }
}

if (patched !== 2) {
  throw new Error(`Expected to patch 2 command permission lambdas, patched ${patched}`);
}

fs.writeFileSync(classFile, buffer);
console.log(`Patched ${patched} command permission lambdas in ${classFile}`);
