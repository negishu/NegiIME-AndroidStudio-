#pragma once
#include ".\\global_utils.h"
class BitArray {
public:
    BitArray(const U08 *image, S32 size) : image_(image), size_(size)
    {
        S32 rank_array_len = (size + 3) / 4;
        rank_array_ = new S32[rank_array_len];
        S32 r = 0;
        const U32 *p = reinterpret_cast<const U32 *>(image_);
        for (S32 i = 0; i < rank_array_len; ++i) {
            rank_array_[i] = r;
            r += popCount(*p);
            ++p;
        }
    }

    ~BitArray()
    {
        delete[] rank_array_;
    }

    const S32 Rank(S32 n)
    {
        S32 idx = n / 32;
        S32 rem = n % 32;
        const U32 *p = reinterpret_cast<const U32 *>(image_);
        S32 rank = rank_array_[idx];
        if (rem) {
            rank += popCount(p[idx] << (32 - rem));
        }
        return rank;
    }

    const U08 GetByte(S32 idx)
    {
        return image_[idx];
    }

private:

    const U08 *image_;

    S32 size_;
    S32 *rank_array_;
};

class SparseArrayImage {

public:
    static const S32 kInvalidValueIndex = -1;
    SparseArrayImage(const U08 *image, const S32 size);
    ~SparseArrayImage();
    const U32 Peek(const U32 index) const;
    const U08 GetValue(const U32 nth) const;
private:
    const S32 ReadInt(const U08 *p) const;
    const U08 *image_;
    const U08 *values_;
    S32 size_;
    S32 num_bits_per_level_;
    S32 num_levels_;
    S32 values_size_;
    std::vector<BitArray *> vectors_;
};
