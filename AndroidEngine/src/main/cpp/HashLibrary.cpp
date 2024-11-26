#include <jni.h>
#include <vector>
#include "picosha2.h"
using namespace std;
using namespace picosha2;

extern "C"
JNIEXPORT jstring JNICALL
Java_com_grupo04_androidengine_AndroidEngine_hash(JNIEnv* env, jobject thiz, jstring data) {
    // thiz para utilizar funciones del objeto que ha llamado a la funcion

    // Reservar memoria, crear una cadena de caracteres, liberar memoria
    jboolean isCopy;
    const char* convertedValue = env->GetStringUTFChars(data, &isCopy);

    vector<unsigned char> hash(32);
    hash256(convertedValue, convertedValue + strlen(convertedValue), hash.begin(), hash.end());

    string hex_str = bytes_to_hex_string(hash.begin(), hash.end());
    env->ReleaseStringUTFChars(data, convertedValue);

    return env->NewStringUTF(hex_str.c_str());
}