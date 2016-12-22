#include "Dictionary.h"

Dictionary::Dictionary()
{
}

Dictionary::~Dictionary(void)
{
}

const U08* Dictionary::GetYomiString(const U32 yomiid, U08& len) const
{
    len = _yomibuflen;
    return (const U08*)_LoudsYomiTrie.Reverse(yomiid, (U08*)_yomibuffer, len);
}

const U08* Dictionary::GetWordString(const U32 wordid, U08& len) const
{
    len = _wordbuflen;
    return (const U08*)_LoudsWordTrie.Reverse(wordid, (U08*)_wordbuffer, len);
}
