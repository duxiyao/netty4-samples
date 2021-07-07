/**
mac
gcc test.c -o test.out -framework Cocoa

windows
g++ test.c -o test.out
g++ test.c -o test.exe
*/

#include <stdio.h>
#include <iostream>
using namespace std;
static void test0(int *p){
    printf("test0:%d.\n",*p);
    *p=9;
    delete p;
}

static void test1(int **p){
    int *s = *p;
    printf("test1:%d.\n",*s);
    *s=10;
    delete p;
}

static void test2(int * &p){
    printf("test2:%d.\n",*p);
    *p=11;
}

static void test3(int p){
    printf("test3:%d.\n",p);
    p=22;
}

int main(int argc, char* argv[])
{
    int a=0;
    test3(a);
    cout<<a<<endl;
    int *i=new int;
    *i=8;
    cout<<i<<endl;
    test0(i);
    cout<<i<<endl;
    printf("main1:%d.\n",*i);
    test1(&i);
    cout<<i<<endl;
    test2(i);
    cout<<i<<endl;
    printf("main2:%d.\n",*i);
    delete i;
    return 0;
}