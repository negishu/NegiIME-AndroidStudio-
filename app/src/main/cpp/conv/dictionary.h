#pragma once

#include "./global_utils.h"
#include "./utils.h"

#include "./loudstrie.h"
#include "./bitarray.h"

class Dictionary
{
public:
    class Candidate {
    public:
        U16 ct, len, index, wordid, dicid;
        double dist, freq, confidence;
        Candidate() :ct(0), len(0), index(-1), dist(-1), wordid(-1), dicid(-1), freq(0), confidence(0) {};
        static bool WHICH(Candidate& rLeft, Candidate& rRight) {
            if (rLeft.ct == rRight.ct) {
                return ((rLeft.dist) > (rRight.dist));
            }
            return ((rLeft.ct) > (rRight.ct));
        }
    };
    class MakeOutStringCallback {
    public:
        MakeOutStringCallback() {};
        virtual ~MakeOutStringCallback() {}
        virtual bool MakeOutString(std::string& in, std::string& out) const = 0;
    };

    Dictionary();
    virtual ~Dictionary(void);
    const loudstrie& GetWordTrie() const { return _LoudsWordTrie; };
    const loudstrie& GetYomiTrie() const { return _LoudsYomiTrie; };
    const bitvectorbasedarray& GetMidaInfoBitArray() const { return _LoudsMidaToInfo; };
    const bitvectorbasedarray& GetYomiInfoBitArray() const { return _LoudsYomiToInfo; };
    const U08* GetYomiString(const U32 yomiid, U08& len) const;
    const U08* GetWordString(const U32 wordid, U08& len) const;

protected:

    static const int _buflen = 255;
    U08 _buffer[_buflen + 1];

    static const int _yomibuflen = 255;
    U08 _yomibuffer[_yomibuflen + 1];
    static const int _wordbuflen = 255;
    U08 _wordbuffer[_wordbuflen + 1];

    const U08 *_WordPieceToWordInfoDataCnt;

    virtual void Init() {};

    loudstrie _LoudsWordTrie;
    loudstrie _LoudsYomiTrie;
    bitvectorbasedarray _LoudsMidaToInfo;
    bitvectorbasedarray _LoudsYomiToInfo;
};
