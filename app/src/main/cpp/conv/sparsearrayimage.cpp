#include "SparseArrayImage.h"

SparseArrayImage::SparseArrayImage(const U08 *image, S32 size) : image_(image), size_(size)
{
    const U08 *p = image_;

    num_bits_per_level_ = ReadInt(p);
    p += 4;

    values_size_ = ReadInt(p);
    p += 4;

    num_levels_ = 32 / num_bits_per_level_;

    if (32 % num_bits_per_level_ != 0) {

        ++num_levels_;
    }

    const U08 *bytes = p + (num_levels_ * 4);

    for (S32 i = 0; i < num_levels_; ++i) {

        const S32 level_size = ReadInt(p);

        p += 4;

        vectors_.push_back(new BitArray(bytes, level_size));
        bytes += level_size;
    }

    values_ = bytes;
    bytes += values_size_;
}

SparseArrayImage::~SparseArrayImage()
{
    for (size_t i = 0; i < vectors_.size(); ++i) {

        delete vectors_[i];
    }
}

const S32 SparseArrayImage::ReadInt(const U08 *p) const
{
    const S32 *n = reinterpret_cast<const S32 *>(p);

    return *n;
}

const U32 SparseArrayImage::Peek(U32 index) const
{
    U32 byte_offset = 0;

    for (S32 level = 0; level < num_levels_; ++level) {

        U32 shift_count = num_bits_per_level_ * (num_levels_ - level - 1);

        U32 idx = (index >> shift_count) % (1 << num_bits_per_level_);

        BitArray *vector = vectors_[level];

        U08 mask = vector->GetByte(byte_offset);

        if (!(mask & (1 << idx))) {

            return kInvalidValueIndex;
        }

        byte_offset = vector->Rank(byte_offset * 8 + idx);
    }

    return byte_offset;
}

const U08 SparseArrayImage::GetValue(U32 nth) const
{
    return (const U08)(*(reinterpret_cast<const U08 *>(&values_[nth])));
}
