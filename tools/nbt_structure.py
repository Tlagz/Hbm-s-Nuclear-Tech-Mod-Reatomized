"""
Minimal NBT reader/writer for GameTest structure templates.

  python tools/nbt_structure.py dump <file.nbt>
  python tools/nbt_structure.py empty <file.nbt> <sx> <sy> <sz> [dataversion]

"empty" writes an empty template of the given size, like empty_8x4x8.
"""
import gzip, struct, sys

TAG_END, TAG_BYTE, TAG_SHORT, TAG_INT, TAG_LONG, TAG_FLOAT, TAG_DOUBLE, TAG_BYTE_ARRAY, TAG_STRING, TAG_LIST, TAG_COMPOUND, TAG_INT_ARRAY, TAG_LONG_ARRAY = range(13)

def read_tag(f, t):
    if t == TAG_BYTE: return ('b', struct.unpack('>b', f.read(1))[0])
    if t == TAG_SHORT: return ('s', struct.unpack('>h', f.read(2))[0])
    if t == TAG_INT: return ('i', struct.unpack('>i', f.read(4))[0])
    if t == TAG_LONG: return ('l', struct.unpack('>q', f.read(8))[0])
    if t == TAG_FLOAT: return ('f', struct.unpack('>f', f.read(4))[0])
    if t == TAG_DOUBLE: return ('d', struct.unpack('>d', f.read(8))[0])
    if t == TAG_BYTE_ARRAY:
        n = struct.unpack('>i', f.read(4))[0]; return ('ba', f.read(n))
    if t == TAG_STRING:
        n = struct.unpack('>H', f.read(2))[0]; return ('str', f.read(n).decode('utf-8'))
    if t == TAG_LIST:
        et = f.read(1)[0]; n = struct.unpack('>i', f.read(4))[0]
        return ('list', et, [read_tag(f, et) for _ in range(n)])
    if t == TAG_COMPOUND:
        d = {}
        while True:
            ct = f.read(1)[0]
            if ct == TAG_END: break
            n = struct.unpack('>H', f.read(2))[0]; name = f.read(n).decode('utf-8')
            d[name] = (ct, read_tag(f, ct))
        return ('c', d)
    if t == TAG_INT_ARRAY:
        n = struct.unpack('>i', f.read(4))[0]; return ('ia', list(struct.unpack('>%di' % n, f.read(4 * n))))
    raise ValueError(t)

def write_tag(f, t, v):
    kind = v[0]
    if t == TAG_BYTE: f.write(struct.pack('>b', v[1]))
    elif t == TAG_SHORT: f.write(struct.pack('>h', v[1]))
    elif t == TAG_INT: f.write(struct.pack('>i', v[1]))
    elif t == TAG_LONG: f.write(struct.pack('>q', v[1]))
    elif t == TAG_FLOAT: f.write(struct.pack('>f', v[1]))
    elif t == TAG_DOUBLE: f.write(struct.pack('>d', v[1]))
    elif t == TAG_STRING:
        b = v[1].encode('utf-8'); f.write(struct.pack('>H', len(b))); f.write(b)
    elif t == TAG_LIST:
        et, items = v[1], v[2]
        f.write(bytes([et])); f.write(struct.pack('>i', len(items)))
        for it in items: write_tag(f, et, it)
    elif t == TAG_COMPOUND:
        for name, (ct, cv) in v[1].items():
            f.write(bytes([ct])); b = name.encode('utf-8'); f.write(struct.pack('>H', len(b))); f.write(b)
            write_tag(f, ct, cv)
        f.write(bytes([TAG_END]))
    else: raise ValueError(t)

def load(path):
    with gzip.open(path, 'rb') as f:
        t = f.read(1)[0]; n = struct.unpack('>H', f.read(2))[0]; f.read(n)
        return read_tag(f, t)

def show(v, indent=0, limit=6):
    pad = '  ' * indent
    if v[0] == 'c':
        for k, (ct, cv) in v[1].items():
            if cv[0] in ('c', 'list'):
                print(pad + k + ':'); show(cv, indent + 1, limit)
            else: print(pad + k + ' = ' + str(cv[1]))
    elif v[0] == 'list':
        for i, it in enumerate(v[2][:limit]):
            print(pad + '[%d]' % i); show(it, indent + 1, limit)
        if len(v[2]) > limit: print(pad + '... %d entries' % len(v[2]))
    else: print(pad + str(v[1]))

def ints(*xs): return ('list', TAG_INT, [('i', x) for x in xs])

def empty(path, sx, sy, sz, dataversion):
    # like empty_8x4x8: only the size, no blocks (the test runner builds the floor and barrier walls)
    root = ('c', {
        'DataVersion': (TAG_INT, ('i', dataversion)),
        'size': (TAG_LIST, ints(sx, sy, sz)),
        'palette': (TAG_LIST, ('list', TAG_COMPOUND, [('c', {'Name': (TAG_STRING, ('str', 'minecraft:air'))})])),
        'blocks': (TAG_LIST, ('list', TAG_COMPOUND, [])),
        'entities': (TAG_LIST, ('list', TAG_COMPOUND, [])),
    })
    with gzip.open(path, 'wb') as f:
        f.write(bytes([TAG_COMPOUND])); f.write(struct.pack('>H', 0))
        write_tag(f, TAG_COMPOUND, root)

if __name__ == '__main__':
    if sys.argv[1] == 'dump':
        show(load(sys.argv[2]))
    elif sys.argv[1] == 'empty':
        empty(sys.argv[2], int(sys.argv[3]), int(sys.argv[4]), int(sys.argv[5]), int(sys.argv[6]) if len(sys.argv) > 6 else 3955)
