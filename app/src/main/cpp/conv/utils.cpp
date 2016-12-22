#include "utils.h"
#include "codes.h"

static int _wcslen(const wchar_t* p)
{
    int n = 0;
    for (;p[n] != 0; n++);
    return n;
}

static int WLookup(const DoubleArray *array, const unsigned int range[], const unsigned short *key, int len, int *result)
{
    int seekto = 0;
    int n = 0;
    int b = array[0].base;

    const unsigned int lowrange = range[0];
    const unsigned int highrange = range[1];

    unsigned int p = 0;
    unsigned int num = 0;

    *result = -1;

    for (int i = 0; i < len; ++i) {
        p = b;
        n = array[p].base;

        if (static_cast<unsigned int>(b) == array[p].check && n < 0) {
            seekto = i;
            *result = -n - 1;
            ++num;
        }

        p = b + static_cast<unsigned char>((key[i] - lowrange)) + 1;

        if (static_cast<unsigned int>(b) == array[p].check) {

            b = array[p].base;
        }
        else {

            return seekto;
        }
    }

    p = b;
    n = array[p].base;

    if (static_cast<unsigned int>(b) == array[p].check && n < 0) {

        seekto = len;
        *result = -n - 1;
    }

    return seekto;
}

static int ConvertW(const DoubleArray *da, const wchar_t*ctable, const unsigned int range[], const _wstring &input, _wstring &output)
{
    unsigned char success = 1;

    output.empty();

    const unsigned int lowrange = range[0];
    const unsigned int highrange = range[1];

    const unsigned short *begin = (const unsigned short *)(input.c_str());
    const unsigned short *end = (const unsigned short *)(input.c_str() + input.length());

    while (begin < end) {

        if (lowrange <= *begin && *begin <= highrange) {

            int result = 0;

            unsigned int mblen = WLookup(da, range, begin, static_cast<int>(end - begin), &result);

            if (mblen > 0) {

                const wchar_t *       p = &ctable[result];
                const unsigned int  len = wcslen(p);

                output.append((const wchar_t*)p, len);

                mblen -= static_cast<unsigned int>(p[len + 1]);
                begin += mblen;
            }
            else {

                output.append((const wchar_t*)begin, 1);
                begin += 1;

                success = 0;
            }
        }
        else {

            output.append((const wchar_t*)begin, 1);
            begin += 1;

            success = 0;
        }
    }

    return success;
}

int Utils::HiraganaToRomaji(const _wstring &input, _wstring& output)
{
    return ConvertW(w_hiragana_to_romaji_da, w_hiragana_to_romaji_table, w_hiragana_to_romaji_code_range, input, output);
}

int Utils::RomajiToHiragana(const _wstring &input, _wstring& output)
{
    return ConvertW(w_romaji_to_hiragana_da, w_romaji_to_hiragana_table, w_romaji_to_hiragana_code_range, input, output);
}

int Utils::HiraganaToKatakana(const _wstring &input, _wstring& output)
{
    return ConvertW(w_hiragana_to_katakana_da, w_hiragana_to_katakana_table, w_hiragana_to_katakana_code_range, input, output);
}

int Utils::HiraganaToHalfwidthKatakana(const _wstring &input, _wstring& output)
{
    return ConvertW(w_hiragana_to_halfwidthkatakana_da, w_hiragana_to_halfwidthkatakana_table, w_hiragana_to_halfwidthkatakana_code_range, input, output);
}

int Utils::USC2ToSJIS(const unsigned short* pSource, unsigned char* pOut, unsigned int n)
{
    unsigned int index = 0;

    for (unsigned int nReadIndex = 0; ((index < n) && (*(pSource + nReadIndex) != 0));nReadIndex++) {

        unsigned short c = pSource[nReadIndex];

        const char* p = 0;

        if (0x0000 <= c && c <= 0x04FF) {

            p = __USC2ToSJIS_0000_04FF[c];
        }
        else
            if (0x2000 <= c && c <= 0x26FF) {

                p = __USC2ToSJIS_2000_26FF[c - 0x2000];
            }
            else
                if (0x3000 <= c && c <= 0x33CF) {

                    p = __USC2ToSJIS_3000_33CF[c - 0x3000];
                }
                else
                    if (0x4E00 <= c && c <= 0x9FFF) {

                        p = __USC2ToSJIS_4E00_9FFF[c - 0x4E00];
                    }
                    else
                        if (0xF800 <= c && c <= 0xFFFF) {

                            p = __USC2ToSJIS_F800_FFFF[c - 0xF800];
                        }

        if (p) {

            while (*p) {

                pOut[index++] = *p; p++;
            }
        }
    }

    pOut[index] = 0;

    return index;
}

int Utils::SJISToUSC2(const unsigned char* pSource, unsigned short* pOut, unsigned int n)
{
    unsigned int index = 0;

    for (unsigned int nReadIndex = 0; ((index < n) && (*(pSource + nReadIndex) != 0)); ) {

        const unsigned char c = pSource[nReadIndex++];

        unsigned short C = 0;

        if ((0x80 <= c && c <= 0x9F)) {

            C = c << 8 | pSource[nReadIndex++];

            C = __SJISToUSC2_8000_9FFF[(C - 0x8000)];
        }
        else
            if ((0xE0 <= c && c <= 0xFF)) {

                C = c << 8 | pSource[nReadIndex++];

                C = __SJISToUSC2_E000_FFFF[(C - 0xE000)];
            }
            else
                if ((0x00 <= c && c <= 0xFF)) {

                    C = __SJISToUSC2_00_FF[c];
                }

        pOut[index++] = C;
    }

    pOut[index] = 0;

    return index;
}

int Utils::USC2ToSJIS(const _wstring& src, std::string& des)
{
    unsigned int index = 0;

    const unsigned short* pSource = (const unsigned short*)src.c_str();

    for (unsigned int nReadIndex = 0; (*(pSource + nReadIndex) != 0); nReadIndex++) {

        unsigned short c = pSource[nReadIndex];

        const char* p = 0;

        if (0x0000 <= c && c <= 0x04FF) {

            p = __USC2ToSJIS_0000_04FF[c];
        }
        else
            if (0x2000 <= c && c <= 0x26FF) {

                p = __USC2ToSJIS_2000_26FF[c - 0x2000];
            }
            else
                if (0x3000 <= c && c <= 0x33CF) {

                    p = __USC2ToSJIS_3000_33CF[c - 0x3000];
                }
                else
                    if (0x4E00 <= c && c <= 0x9FFF) {

                        p = __USC2ToSJIS_4E00_9FFF[c - 0x4E00];
                    }
                    else
                        if (0xF800 <= c && c <= 0xFFFF) {

                            p = __USC2ToSJIS_F800_FFFF[c - 0xF800];
                        }

        if (p) {

            while (*p) {

                des += *p; p++; index++;
            }
        }
    }

    return des.length();
}

int Utils::SJISToUSC2(const std::string& src, _wstring& des)
{
    const unsigned char* pSource = (const unsigned char*)src.c_str();

    for (unsigned int nReadIndex = 0; ((*(pSource + nReadIndex) != 0)); ) {

        const unsigned char c = pSource[nReadIndex++];

        unsigned short C = 0;

        if ((0x80 <= c && c <= 0x9F)) {

            C = c << 8 | pSource[nReadIndex++];

            C = __SJISToUSC2_8000_9FFF[(C - 0x8000)];
        }
        else
            if ((0xE0 <= c && c <= 0xFF)) {

                C = c << 8 | pSource[nReadIndex++];

                C = __SJISToUSC2_E000_FFFF[(C - 0xE000)];
            }
            else
                if ((0x00 <= c && c <= 0xFF)) {

                    C = __SJISToUSC2_00_FF[c];
                }

        des += C;
    }

    return des.length();
}

int Utils::USC2ToUTF8(const unsigned short* pSource, unsigned char* pOut, unsigned int n)
{
    unsigned int index = 0;

    for (unsigned int nReadIndex = 0; ((index < n) && (*(pSource + nReadIndex) != 0)); nReadIndex++) {

        if (*pSource <= 0x007F) {

            pOut[index++] = ((*(pSource + nReadIndex) & 0x007F));
        }
        else
            if (*pSource <= 0x07ff) {

                pOut[index++] = ((*(pSource + nReadIndex) & 0x07C0) >> 6) | 0xC0;
                pOut[index++] = ((*(pSource + nReadIndex) & 0x003F)) | 0x80;
            }
            else {

                pOut[index++] = ((*(pSource + nReadIndex) & 0xF000) >> 12) | 0xE0;
                pOut[index++] = ((*(pSource + nReadIndex) & 0x0FC0) >> 6) | 0x80;
                pOut[index++] = ((*(pSource + nReadIndex) & 0x003F)) | 0x80;
            }
    }

    pOut[index] = 0;

    return index;
}

int Utils::UTF8ToUSC2(const unsigned char* pSource, unsigned short* pOut, unsigned int n)
{
    unsigned int index = 0;

    for (unsigned int nReadIndex = 0; ((index < n) && (*(pSource + nReadIndex) != 0)); ) {

        if ((*(pSource + nReadIndex) & 0x80) == 0x00) {

            pOut[index++] = ((*(pSource + nReadIndex) & 0x7F));

            nReadIndex++;
        }
        else
            if ((*(pSource + nReadIndex) & 0xE0) == 0xC0) {

                pOut[index++] = (((*(pSource + nReadIndex) & 0x3F) << 6) | (*(pSource + nReadIndex + 1) & 0x003F));

                nReadIndex += 2;
            }
            else {

                pOut[index++] = (((*(pSource + nReadIndex) & 0x3F) << 12) | ((*(pSource + nReadIndex + 1) & 0x003F) << 6) | (*(pSource + nReadIndex + 2) & 0x003F));

                nReadIndex += 3;
            }
    }

    pOut[index] = 0;

    return index;
}

int Utils::USC2ToUTF8(const _wstring& src, std::string& des)
{
    const unsigned short* pSource = (const unsigned short*)src.c_str();

    for (unsigned int nReadIndex = 0; (*(pSource + nReadIndex) != 0); nReadIndex++) {

        if (*(pSource + nReadIndex) <= 0x007f) {

            des += ((*(pSource + nReadIndex) & 0x007f));
        }
        else
            if (*(pSource + nReadIndex) <= 0x07ff) {

                des += ((*(pSource + nReadIndex) & 0x07c0) >> 6) | 0xc0;
                des += ((*(pSource + nReadIndex) & 0x003f)) | 0x80;
            }
            else {

                des += ((*(pSource + nReadIndex) & 0xf000) >> 12) | 0xe0;
                des += ((*(pSource + nReadIndex) & 0x0fc0) >> 6) | 0x80;
                des += ((*(pSource + nReadIndex) & 0x003f)) | 0x80;
            }
    }

    return des.length();
}

int Utils::UTF8ToUSC2(const std::string& src, _wstring& des)
{
    const unsigned char* pSource = (const unsigned char*)src.c_str();

    for (unsigned int nReadIndex = 0; (*(pSource + nReadIndex) != 0); ) {

        if ((*(pSource + nReadIndex) & 0x80) == 0x00) {

            des += ((*(pSource + nReadIndex) & 0x7F));

            nReadIndex++;
        }
        else
            if ((*(pSource + nReadIndex) & 0xE0) == 0xC0) {

                des += (((*(pSource + nReadIndex) & 0x3F) << 6) | (*(pSource + nReadIndex + 1) & 0x003F));

                nReadIndex += 2;
            }
            else {

                des += (((*(pSource + nReadIndex) & 0x3F) << 12) | ((*(pSource + nReadIndex + 1) & 0x003F) << 6) | (*(pSource + nReadIndex + 2) & 0x003F));

                nReadIndex += 3;
            }
    }

    return des.length();
}

int Utils::SJISToUTF8(const unsigned char* pSource, unsigned char* pOut, unsigned int n)
{
    unsigned int index = 0;

    for (unsigned int nReadIndex = 0; ((index < n) && (*(pSource + nReadIndex) != 0)); ) {

        const unsigned char c = pSource[nReadIndex++];

        unsigned short C = 0;

        if ((0x80 <= c && c <= 0x9F)) {

            C = c << 8 | pSource[nReadIndex++];

            C = __SJISToUSC2_8000_9FFF[(C - 0x8000)];
        }
        else
            if ((0xE0 <= c && c <= 0xFF)) {

                C = c << 8 | pSource[nReadIndex++];

                C = __SJISToUSC2_E000_FFFF[(C - 0xE000)];
            }
            else
                if ((0x00 <= c && c <= 0xFF)) {

                    C = __SJISToUSC2_00_FF[c];
                }

        if (C <= 0x007F) {

            pOut[index++] = ((C & 0x007F));
        }
        else
            if (C <= 0x07FF) {

                pOut[index++] = ((C & 0x07C0) >> 6) | 0xC0;
                pOut[index++] = ((C & 0x003F)) | 0x80;
            }
            else {

                pOut[index++] = ((C & 0xF000) >> 12) | 0xE0;
                pOut[index++] = ((C & 0x0FC0) >> 6) | 0x80;
                pOut[index++] = ((C & 0x003F)) | 0x80;
            }
    }

    pOut[index] = 0;

    return index;
}

int Utils::UTF8ToSJIS(const unsigned char* pSource, unsigned char* pOut, unsigned int n)
{
    unsigned int index = 0;

    for (unsigned int nReadIndex = 0; ((index < n) && (*(pSource + nReadIndex) != 0)); ) {

        unsigned short C = 0;

        if ((*(pSource + nReadIndex) & 0x80) == 0x00) {

            C = ((*(pSource + nReadIndex) & 0x7f));

            nReadIndex++;
        }
        else
            if ((*(pSource + nReadIndex) & 0xE0) == 0xc0) {

                C = (((*(pSource + nReadIndex) & 0x3f) << 6) | (*(pSource + nReadIndex + 1) & 0x003f));

                nReadIndex += 2;
            }
            else {

                C = (((*(pSource + nReadIndex) & 0x3f) << 12) | ((*(pSource + nReadIndex + 1) & 0x003f) << 6) | (*(pSource + nReadIndex + 2) & 0x003f));

                nReadIndex += 3;
            }

            const char* p = 0;

            if (0x0000 <= C && C <= 0x04FF) {

                p = __USC2ToSJIS_0000_04FF[C];
            }
            else
                if (0x2000 <= C && C <= 0x26FF) {

                    p = __USC2ToSJIS_2000_26FF[C - 0x2000];
                }
                else
                    if (0x3000 <= C && C <= 0x33CF) {

                        p = __USC2ToSJIS_3000_33CF[C - 0x3000];
                    }
                    else
                        if (0x4E00 <= C && C <= 0x9FFF) {

                            p = __USC2ToSJIS_4E00_9FFF[C - 0x4E00];
                        }
                        else
                            if (0xF800 <= C && C <= 0xFFFF) {

                                p = __USC2ToSJIS_F800_FFFF[C - 0xF800];
                            }

            if (p) {

                while (*p) {

                    pOut[index++] = *p; p++;
                }
            }
    }

    pOut[index] = 0;

    return index;
}

int Utils::SJISToUTF8(const std::string& src, std::string& dst)
{
    const unsigned char* pSource = (const unsigned char*)src.c_str();

    for (unsigned int nReadIndex = 0; (*(pSource + nReadIndex) != 0); ) {

        const unsigned char c = pSource[nReadIndex++];

        unsigned short C = 0;

        if ((0x80 <= c && c <= 0x9F)) {

            C = c << 8 | pSource[nReadIndex++];

            C = __SJISToUSC2_8000_9FFF[(C - 0x8000)];
        }
        else
            if ((0xE0 <= c && c <= 0xFF)) {

                C = c << 8 | pSource[nReadIndex++];

                C = __SJISToUSC2_E000_FFFF[(C - 0xE000)];
            }
            else
                if ((0x00 <= c && c <= 0xFF)) {

                    C = __SJISToUSC2_00_FF[c];
                }

        if (C <= 0x007F) {

            dst += ((C & 0x007F));
        }
        else
            if (C <= 0x07FF) {

                dst += ((C & 0x07C0) >> 6) | 0xC0;
                dst += ((C & 0x003F)) | 0x80;
            }
            else {

                dst += ((C & 0xF000) >> 12) | 0xE0;
                dst += ((C & 0x0FC0) >> 6) | 0x80;
                dst += ((C & 0x003F)) | 0x80;
            }
    }

    return dst.length();
}

int Utils::UTF8ToSJIS(const std::string& src, std::string& dst)
{
    const unsigned char* pSource = (const unsigned char*)src.c_str();

    for (unsigned int nReadIndex = 0; (*(pSource + nReadIndex) != 0); ) {

        unsigned short C = 0;

        if ((*(pSource + nReadIndex) & 0x80) == 0x00) {

            C = ((*(pSource + nReadIndex) & 0x7f));

            nReadIndex++;
        }
        else
            if ((*(pSource + nReadIndex) & 0xE0) == 0xc0) {

                C = (((*(pSource + nReadIndex) & 0x3f) << 6) | (*(pSource + nReadIndex + 1) & 0x003f));

                nReadIndex += 2;
            }
            else {

                C = (((*(pSource + nReadIndex) & 0x3f) << 12) | ((*(pSource + nReadIndex + 1) & 0x003f) << 6) | (*(pSource + nReadIndex + 2) & 0x003f));

                nReadIndex += 3;
            }

            const char* p = 0;

            if (0x0000 <= C && C <= 0x04FF) {

                p = __USC2ToSJIS_0000_04FF[C];
            }
            else
                if (0x2000 <= C && C <= 0x26FF) {

                    p = __USC2ToSJIS_2000_26FF[C - 0x2000];
                }
                else
                    if (0x3000 <= C && C <= 0x33CF) {

                        p = __USC2ToSJIS_3000_33CF[C - 0x3000];
                    }
                    else
                        if (0x4E00 <= C && C <= 0x9FFF) {

                            p = __USC2ToSJIS_4E00_9FFF[C - 0x4E00];
                        }
                        else
                            if (0xF800 <= C && C <= 0xFFFF) {

                                p = __USC2ToSJIS_F800_FFFF[C - 0xF800];
                            }

            if (p) {

                while (*p) {

                    dst += *p; p++;
                }
            }
    }

    return dst.length();
}

int Utils::USC2ToValue(const _wstring &input, std::string& out)
{
    const unsigned short* pSource = (const unsigned short*)input.c_str();

    for (unsigned int nReadIndex = 0; (*(pSource + nReadIndex) != 0); ) {

        unsigned short wc = *(pSource + nReadIndex);

        U32 offset = 0;

        if ((wc >= 0x0001 && wc <= 0x001f) || (wc >= 0x3041 && wc <= 0x305f)) {
            offset = 0x3041 - 0x0001;
        }
        else if ((wc >= 0x0040 && wc <= 0x0075) || (wc >= 0x3060 && wc <= 0x3095)) {
            offset = 0x3060 - 0x0040;
        }
        else if ((wc >= 0x0076 && wc <= 0x0077) || (wc >= 0x30FB && wc <= 0x30FC)) {
            offset = 0x30FB - 0x0076;
        }
        else {
            out = "";
            return 0;
        }

        wc -= offset;

        out += wc & 0xFF;

        nReadIndex++;
    }

    return out.length();
}

int Utils::ValueToUSC2(const std::string& input, _wstring &out)
{
    const unsigned char* pSource = (const unsigned char*)input.c_str();

    const int iLen = input.length();

    for (int nReadIndex = 0; nReadIndex < iLen; ) {

        unsigned short wc = (unsigned short)*(pSource + nReadIndex);

        U32 offset = 0;

        if ((wc >= 0x0001 && wc <= 0x001f)) {
            offset = 0x3041 - 0x0001;
        }
        else if ((wc >= 0x0040 && wc <= 0x0075)) {
            offset = 0x3060 - 0x0040;
        }
        else if ((wc >= 0x0076 && wc <= 0x0077)) {
            offset = 0x30FB - 0x0076;
        }

        wc += offset;

        out += wc;

        nReadIndex++;
    }

    return out.length();
}

int Utils::DigitsToValue(const _wstring &input, std::string& out)
{
    const unsigned short* pSource = (const unsigned short*)input.c_str();

    if (input.length() == 1) out += "00";
    if (input.length() == 2) out += "0";

    for (unsigned int nReadIndex = 0; (*(pSource + nReadIndex) != 0); ) {

        unsigned short wc = *(pSource + nReadIndex);

        if (0x30 <= wc && wc <= 0x39) {

            unsigned char c = (unsigned char)(wc);

            out += c;
        }

        nReadIndex++;
    }

    return out.length();
}

int Utils::ValueToDigits(const std::string& input, _wstring &out)
{
    const unsigned char* pSource = (const unsigned char*)input.c_str();

    for (unsigned int nReadIndex = 0; (*(pSource + nReadIndex) != 0); ) {

        unsigned char c = *(pSource + nReadIndex);

        if (2 <= c && c < 86) {

            unsigned short wc = (unsigned short)((c - 2) + 0x3041);

            out += wc;
        }
        else
            if (c == 1) {

                unsigned short wc = 0x30FC;

                out += wc;
            }

        nReadIndex++;
    }

    return out.length();
}

int Utils::DigitsToDigits(const _wstring &input, _wstring &out)
{
    const unsigned short* pSource = (const unsigned short*)input.c_str();

    if (input.length() == 1) out += (const unsigned short *)L"00";
    if (input.length() == 2) out += (const unsigned short *)L"0";

    for (unsigned int nReadIndex = 0; (*(pSource + nReadIndex) != 0); ) {

        unsigned short wc = *(pSource + nReadIndex);

        if (0x30 <= wc && wc <= 0x39) {

            unsigned char c = (unsigned char)(wc);

            out += c;
        }

        nReadIndex++;
    }

    return out.length();
}
