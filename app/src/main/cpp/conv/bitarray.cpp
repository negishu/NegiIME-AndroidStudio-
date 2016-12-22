#include "bitarray.h"

void bitvectorbasedarray::Open(const U08 *image)
{
    const int index_length = Read32(image);
    const int base_length = Read32(image + 4);
    const int step_length = Read32(image + 8);
    const int num_entries = Read32(image + 12);

    index_.Init(image + 16, index_length);

    base_length_ = base_length;
    step_length_ = step_length;
    num_entries_ = num_entries;

    data_ = reinterpret_cast<const U08*>(image + 16 + index_length);
}

void bitvectorbasedarray::Close()
{
    base_length_ = 0;
    step_length_ = 0;

    data_ = 0;
}

const U08 *bitvectorbasedarray::Get(const U32 index, U32& wLength, U08& uStep) const
{
    wLength = 0;

    if (num_entries_ <= index) return 0;

    const U32 bit_index = index_.Select0(index);
    const U32 data_index = base_length_ * (index)+step_length_ * (index_.Rank1(bit_index));

    U32 i = bit_index;

    while (index_.Get(i)) ++i;

    wLength = base_length_ + step_length_ * (i - bit_index);

    uStep = base_length_;

    return data_ + data_index;
}
