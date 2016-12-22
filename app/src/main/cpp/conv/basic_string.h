#ifndef WSTRING
#define WSTRING
#include "global_utils.h"
class _wstring
{
public:
    _wstring() {
        _size = 0; _str[_size] = 0;
    };
    _wstring(const unsigned short* in) {
        _size = 0;
        while (in[_size] != 0) {
            _str[_size] = in[_size]; _size++;
        }
        _str[_size] = 0;
    };
    virtual ~_wstring() {};
    void empty() {
        _size = 0; _str[_size] = 0;
    };
    const unsigned short * c_str() const { return _str; };
    const unsigned int    length() const { return _size; };
    void append(const wchar_t* add, const unsigned int len) {
        unsigned int n = 0;
        while (add[n] != 0 && n < len) {
            _str[_size] = add[n]; _size++; n++;
        }
        _str[_size] = 0;
    };
    _wstring& operator+= (const unsigned short* s) {
        int n = 0;
        while (s[n] != 0) {
            _str[_size] = s[n]; _size++; n++;
        }
        _str[_size] = 0;
        return *this;
    };
    _wstring& operator += (const unsigned short c) {
        _str[_size++] = c;
        _str[_size] = 0;
        return *this;
    };
    static const int _MAX_ = 1024;
    int _size;
    U16 _str[_MAX_];
};
#endif