const fs = require("fs");

const file = process.argv[2];
if (!file) {
  throw new Error("Usage: node tools/patch-mtr-falldistance.js <MTR.class>");
}

const original = fs.readFileSync(file);
const cpCount = original.readUInt16BE(8);
let offset = 10;
const cp = new Array(cpCount);

function readU2(position) {
  return original.readUInt16BE(position);
}

function writeU2(buffer, position, value) {
  buffer.writeUInt16BE(value, position);
}

for (let i = 1; i < cpCount; i++) {
  const tag = original[offset++];
  const entry = { tag, offset: offset - 1 };
  cp[i] = entry;
  switch (tag) {
    case 1: {
      const length = readU2(offset);
      entry.lengthOffset = offset;
      entry.bytesOffset = offset + 2;
      entry.value = original.toString("utf8", offset + 2, offset + 2 + length);
      offset += 2 + length;
      break;
    }
    case 3:
    case 4:
      offset += 4;
      break;
    case 5:
    case 6:
      offset += 8;
      i++;
      break;
    case 7:
    case 8:
    case 16:
    case 19:
    case 20:
      entry.index = readU2(offset);
      offset += 2;
      break;
    case 9:
    case 10:
    case 11:
    case 12:
    case 17:
    case 18:
      entry.index1 = readU2(offset);
      entry.index2 = readU2(offset + 2);
      entry.index2Offset = offset + 2;
      offset += 4;
      break;
    case 15:
      offset += 3;
      break;
    default:
      throw new Error(`Unsupported constant pool tag ${tag} at #${i}`);
  }
}

const cpEnd = offset;
const utf8Index = (value) => cp.findIndex((entry) => entry && entry.tag === 1 && entry.value === value);
const className = (index) => cp[cp[index].index].value;
const nameAndType = (index) => {
  const entry = cp[index];
  return { name: cp[entry.index1].value, descriptor: cp[entry.index2].value, descriptorIndex: entry.index2, descriptorOffset: entry.index2Offset };
};

let fallDistanceFieldRef = -1;
let fallDistanceNameAndType = null;
for (let i = 1; i < cpCount; i++) {
  const entry = cp[i];
  if (!entry || entry.tag !== 9) {
    continue;
  }
  const owner = className(entry.index1);
  const nat = nameAndType(entry.index2);
  if (owner === "net/minecraft/class_3222" && nat.name === "field_6017" && nat.descriptor === "F") {
    fallDistanceFieldRef = i;
    fallDistanceNameAndType = entry.index2;
    break;
  }
}

if (fallDistanceFieldRef < 0) {
  throw new Error("Could not find ServerPlayer.field_6017:F field reference");
}

let classOffset = cpEnd;
classOffset += 6;
const interfacesCount = readU2(classOffset);
classOffset += 2 + interfacesCount * 2;
const fieldsCount = readU2(classOffset);
classOffset += 2;
for (let i = 0; i < fieldsCount; i++) {
  classOffset += 6;
  const attributesCount = readU2(classOffset);
  classOffset += 2;
  for (let j = 0; j < attributesCount; j++) {
    classOffset += 2;
    const length = original.readUInt32BE(classOffset);
    classOffset += 4 + length;
  }
}

const methodsCount = readU2(classOffset);
classOffset += 2;
const codeNameIndex = utf8Index("Code");
const updatePlayerNameIndex = utf8Index("updatePlayer");
const updatePlayerDescriptorIndex = utf8Index("(Lnet/minecraft/class_3222;Z)V");

let codeOffset = -1;
for (let i = 0; i < methodsCount; i++) {
  classOffset += 2;
  const nameIndex = readU2(classOffset);
  const descriptorIndex = readU2(classOffset + 2);
  classOffset += 4;
  const attributesCount = readU2(classOffset);
  classOffset += 2;
  for (let j = 0; j < attributesCount; j++) {
    const attributeNameIndex = readU2(classOffset);
    const length = original.readUInt32BE(classOffset + 2);
    if (nameIndex === updatePlayerNameIndex && descriptorIndex === updatePlayerDescriptorIndex && attributeNameIndex === codeNameIndex) {
      codeOffset = classOffset + 6;
    }
    classOffset += 6 + length;
  }
}

if (codeOffset < 0) {
  throw new Error("Could not find updatePlayer Code attribute");
}

const newUtf8 = Buffer.from([1, 0, 1, 0x44]);
const patched = Buffer.concat([original.subarray(0, 8), Buffer.alloc(2), original.subarray(10, cpEnd), newUtf8, original.subarray(cpEnd)]);
writeU2(patched, 8, cpCount + 1);
const shift = newUtf8.length;

const newDescriptorIndex = cpCount;
writeU2(patched, cp[fallDistanceNameAndType].index2Offset, newDescriptorIndex);
const patchedCodeOffset = codeOffset + shift;
writeU2(patched, patchedCodeOffset, Math.max(3, patched.readUInt16BE(patchedCodeOffset)));

const codeLength = patched.readUInt32BE(patchedCodeOffset + 4);
const instructionsOffset = patchedCodeOffset + 8;
let changedOpcode = false;
for (let i = instructionsOffset; i < instructionsOffset + codeLength - 4; i++) {
  if (
    patched[i] === 0x2a &&
    patched[i + 1] === 0x0b &&
    patched[i + 2] === 0xb5 &&
    patched.readUInt16BE(i + 3) === fallDistanceFieldRef
  ) {
    patched[i + 1] = 0x0e;
    changedOpcode = true;
    break;
  }
}

if (!changedOpcode) {
  throw new Error("Could not find aload_0/fconst_0/putfield field_6017 instruction sequence");
}

fs.writeFileSync(file, patched);
