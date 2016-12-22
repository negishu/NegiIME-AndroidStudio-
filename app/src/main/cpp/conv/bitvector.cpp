#include "bitvector.h"

bitvector::bitvector() :index(0), levelA(0), levelB(0)
{
}

bitvector::~bitvector()
{
    delete[] levelB;
    delete[] levelA;
    delete[] index;
}

void bitvector::Init(const U08 *data, U32 bytes) {

    U32 total = 0;

    B = (U32*)data;

    n_bytes = bytes;
    n_bites = bytes * 8;

    index_len = get_index_len((n_bytes + BSIZE - 1) / BSIZE);
    index = new U32[index_len];

    for (U32 i = 0; i < index_len; ++i) {

        total += count_bits_in_chunk(i, n_bytes);
        index[i] = total;
    }

    levelA_size = n_bites / LEVELA + 1;
    levelB_size = n_bites / LEVELB + 1;

    levelA = new U32[levelA_size];
    levelB = new U08[levelB_size];

    U32 r = 0;

    for (U32 i = 0; i <= n_bites; i++) {

        if (i % LEVELA == 0) levelA[i / LEVELA] = r;
        if (i % LEVELB == 0) levelB[i / LEVELB] = r - levelA[i / LEVELA];
        if (i != n_bites && i % BSIZE == 0) r += popCount(B[i / BSIZE]);
    }
}

const U32 bitvector::Select0(U32 n) const {

    U32 c = FindChunk0(n);

    U32 total = 0;

    if (c) {

        total = GetTotal0(c - 1);
    }

    return SelectNaive0(c * BSIZE * 8, n - total);
}

const U32 bitvector::Select1(U32 n) const {

    U32 c = FindChunk1(n);

    U32 total = 0;

    if (c) {

        total = GetTotal1(c - 1);
    }

    return SelectNaive1(c * BSIZE * 8, n - total);
}

const U32 bitvector::Get(U32 pos) const {

    const U32 blockPos = pos / BSIZE;
    const U32 offset = pos%BSIZE;

    return ((B[blockPos] >> offset) & 1);
}

const U32 bitvector::Rank0(U32 pos) const {

    return pos - _Rank(pos);
}

const U32 bitvector::Rank1(U32 pos) const {

    return _Rank(pos);
}

const U32 bitvector::_Rank(U32 pos) const {

    U32 remain = pos % LEVELB;
    U32 blockP = remain / BSIZE;
    U32 remainP = remain%BSIZE;

    U32 r = levelA[pos / LEVELA] + levelB[pos / LEVELB];

    r += popCount(B[pos / BSIZE] & ((1 << remainP) - 1));

    return r;
}

const U32 bitvector::GetTotal0(U32 i) const {

    return BSIZE * 8 * (i + 1) - index[i];
}

const U32 bitvector::FindChunk0(U32 n) const {

    U32 left = 0, right = index_len, mid = 0;

    while (left < right) {

        mid = (left + right) / 2;

        if (GetTotal0(mid) >= n) right = mid;
        else                     left = mid + 1;
    }

    return mid;
}

const U32 bitvector::GetTotal1(U32 i) const {

    return index[i];
}

const U32 bitvector::FindChunk1(U32 n) const {

    U32 left = 0, right = index_len, mid = 0;

    while (left < right) {

        mid = (left + right) / 2;

        if (GetTotal1(mid) >= n) right = mid;
        else                     left = mid + 1;
    }

    return mid;
}

const U32 bitvector::SelectNaive0(U32 start, U32 n) const {

    U32 i = start;

    U32* p = (U32*)&B[i / BSIZE];

    do {

        U32 bc = BitCount0(*p);

        if (bc > n) break;

        n -= bc; i += BSIZE;

        ++p;
    } while (1);

    U32 w = *p;

    while (1) {

        if (!(w & 1)) {

            if (n == 0) {
                ++i;
                break;
            }

            --n;
        }

        ++i; w >>= 1;
    }

    return i;
}

const U32 bitvector::SelectNaive1(U32 start, U32 n) const {

    U32 i = start;

    U32* p = (U32*)&B[i / BSIZE];

    do {

        U32 bc = BitCount1(*p);

        if (bc > n) break;

        n -= bc; i += BSIZE;

        ++p;
    } while (1);

    U32 w = *p;

    while (1) {

        if ((w & 1)) {

            if (n == 0) {
                break;
            }

            --n;
        }

        ++i; w >>= 1;
    }

    return i;
}

const U32 bitvector::count_bits_in_chunk(U32 c, U32 limit) {

    U32 *p = (U32 *)& B[c * 8];

    limit -= BSIZE * c;
    limit /= sizeof(S32);

    U32 nr = 0, ne = BSIZE / sizeof(U32);

    for (U32 i = 0; i < ne && i < limit; ++i) {

        nr += BitCount1(*p); ++p;
    }

    return nr;
}

const U32 bitvector::get_index_len(U32 n) {

    U32 i;

    for (i = 1; i < n; i *= 2);

    return i;
}
