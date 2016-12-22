#ifndef GLOBAL_UTILS
#define GLOBAL_UTILS

#include <string>
#include <vector>
#include <map>
#include <queue>

typedef char            S08;
typedef short           S16;
typedef int             S32;
typedef unsigned char   U08;
typedef unsigned short  U16;
typedef unsigned int    U32;

static const U08 popCountArray[] = {
0,1,1,2,1,2,2,3,1,2,2,3,2,3,3,4,1,2,2,3,2,3,3,4,2,3,3,4,3,4,4,5,
1,2,2,3,2,3,3,4,2,3,3,4,3,4,4,5,2,3,3,4,3,4,4,5,3,4,4,5,4,5,5,6,
1,2,2,3,2,3,3,4,2,3,3,4,3,4,4,5,2,3,3,4,3,4,4,5,3,4,4,5,4,5,5,6,
2,3,3,4,3,4,4,5,3,4,4,5,4,5,5,6,3,4,4,5,4,5,5,6,4,5,5,6,5,6,6,7,
1,2,2,3,2,3,3,4,2,3,3,4,3,4,4,5,2,3,3,4,3,4,4,5,3,4,4,5,4,5,5,6,
2,3,3,4,3,4,4,5,3,4,4,5,4,5,5,6,3,4,4,5,4,5,5,6,4,5,5,6,5,6,6,7,
2,3,3,4,3,4,4,5,3,4,4,5,4,5,5,6,3,4,4,5,4,5,5,6,4,5,5,6,5,6,6,7,
3,4,4,5,4,5,5,6,4,5,5,6,5,6,6,7,4,5,5,6,5,6,6,7,5,6,6,7,6,7,7,8,
};

inline const U32 popCount(U32 r) {
    return popCountArray[(r >> 24) & 0xFF] + popCountArray[(r >> 16) & 0xFF] + popCountArray[(r >> 8) & 0xFF] + popCountArray[(r)& 0xFF];
}
inline const U32 BitCount1(U32 x) {
    return popCount(x);
}
inline const U32 BitCount0(U32 x) {
    return 32 - popCount(x);
}

inline const U08 ABS08(S08 i)
{
    if (i < 0) return -i;
    else       return  i;
}

inline const U16 ABS16(S16 i)
{
    if (i < 0) return -i;
    else       return  i;
}

inline const U32 ABS32(S32 i)
{
    if (i < 0) return -i;
    else       return  i;
}

inline const U32 Read32(const U08 *data)
{
    return *reinterpret_cast<const U32*>(data);
}

template<class T> class scoped_array {

public:

    typedef T element_type;

    explicit scoped_array(T * p = 0) : ptr_(p) {}

    ~scoped_array() {

        enum { type_must_be_complete = sizeof(T) };

        delete[] ptr_;
    }

    void reset(T* p = 0) {

        if (p != ptr_) {

            enum { type_must_be_complete = sizeof(T) };

            delete[] ptr_;

            ptr_ = p;
        }
    }

    T * get() const {

        return ptr_;
    }

    T & operator[](U32 i) const {

        return ptr_[i];
    }

    void swap(scoped_array& p2) {

        T* tmp = ptr_;
        ptr_ = p2.ptr_;
        p2.ptr_ = tmp;
    }

    T * release() {

        T* ret = ptr_;

        ptr_ = 0;

        return ret;
    }

    bool operator==(T * p) const { return ptr_ == p; }
    bool operator!=(T * p) const { return ptr_ != p; }

private:

    T * ptr_;

    scoped_array(scoped_array const &);
    scoped_array & operator= (scoped_array const &);

    template<class T2> bool operator==(scoped_array<T2> const & p2) const;
    template<class T2> bool operator!=(scoped_array<T2> const & p2) const;
};

template<class T> class scoped_ptr {

private:

    T * ptr_;

    scoped_ptr(scoped_ptr const &);
    scoped_ptr & operator= (scoped_ptr const &);

    template<class T2> bool operator==(scoped_ptr<T2> const & p2) const;
    template<class T2> bool operator!=(scoped_ptr<T2> const & p2) const;

public:

    typedef T element_type;

    explicit scoped_ptr(T * p = 0) : ptr_(p) {}

    ~scoped_ptr() {

        enum { type_must_be_complete = sizeof(T) };

        delete ptr_;
    }

    T & operator*() const {

        return *ptr_;
    }

    T * operator->() const {

        return ptr_;
    }

    T * get() const { return ptr_; }

    void reset(T * p = 0) {

        if (p != ptr_) {

            enum { type_must_be_complete = sizeof(T) };

            delete ptr_;

            ptr_ = p;
        }
    }

    void swap(scoped_ptr & p2) {

        T* tmp = ptr_;

        ptr_ = p2.ptr_;

        p2.ptr_ = tmp;
    }

    T * release() {

        T* ret = ptr_;

        ptr_ = 0;

        return ret;
    }

    bool operator==(T * p) const { return ptr_ == p; }
    bool operator!=(T * p) const { return ptr_ != p; }
};

inline static std::vector<std::string> Split(std::string s, U08 c, std::vector<std::string>& v) {

    for (size_t p = 0; (p = s.find(c)) != s.npos; ) {

        v.push_back(s.substr(0, p));
        s = s.substr(p + 1);
    }

    v.push_back(s);

    return v;
}

#endif //GLOBAL_UTILS
