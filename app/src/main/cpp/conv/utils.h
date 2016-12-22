#pragma once

#include <string>

#include "basic_string.h"

class Utils
{
public:

    static int RomajiToHiragana(const _wstring &input, _wstring& output);
    static int HiraganaToRomaji(const _wstring &input, _wstring& output);
    static int HiraganaToKatakana(const _wstring &input, _wstring& output);
    static int HiraganaToHalfwidthKatakana(const _wstring &input, _wstring& output);

    static int USC2ToSJIS(const unsigned short* pSource, unsigned char*  pOut, unsigned int n);
    static int SJISToUSC2(const unsigned char*  pSource, unsigned short* pOut, unsigned int n);

    static int USC2ToSJIS(const _wstring& src, std::string& des);
    static int SJISToUSC2(const std::string& src, _wstring& des);

    static int USC2ToUTF8(const unsigned short* pSource, unsigned char*  pOut, unsigned int n);
    static int UTF8ToUSC2(const unsigned char*  pSource, unsigned short* pOut, unsigned int n);

    static int USC2ToUTF8(const _wstring& src, std::string& des);
    static int UTF8ToUSC2(const std::string& src, _wstring& des);

    static int SJISToUTF8(const unsigned char* pSource, unsigned char* pOut, unsigned int n);
    static int UTF8ToSJIS(const unsigned char* pSource, unsigned char* pOut, unsigned int n);

    static int SJISToUTF8(const std::string& src, std::string& dst);
    static int UTF8ToSJIS(const std::string& src, std::string& dst);

    static int USC2ToValue(const _wstring &input, std::string& out);
    static int ValueToUSC2(const std::string& out, _wstring &input);

    static int DigitsToValue(const _wstring &input, std::string& out);
    static int ValueToDigits(const std::string& out, _wstring &input);

    static int DigitsToDigits(const _wstring &input, _wstring &out);
};
