#include <stdio.h>

#include <jni.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/ioctl.h>
#include <netinet/in.h>
#include <net/if.h>
#include <unistd.h>
//#include <JNIHelp.h>
#include <android/log.h>


#define LOG_TAG "getip"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

#define NELEM(a) ((int)(sizeof(a)/sizeof((a)[0])))

typedef struct name_pattern_t{
	const char* name;
	uint32_t len;
} name_pattern;


static name_pattern patterns [] = {
		{"wlan", 4},
		{"wl", 2},
};

static jint getPrivateCTypeNetwork(struct ifconf &ifc)
{
    struct ifreq* ifr;
	int n;
    char* ip_addr = NULL;
	jint ret = 0;

	ifr = ifc.ifc_req;
	for (n = 0; n < ifc.ifc_len; n += sizeof(struct ifreq)) {
		ip_addr = (char*)(&((struct sockaddr_in *)&ifr->ifr_addr)->sin_addr);
		LOGD("%s: %d.%d.%d.%d\n", ifr->ifr_name ? ifr->ifr_name : "NO Name",
				ip_addr[0],
				ip_addr[1],
				ip_addr[2],
				ip_addr[3]);

		if (192 == ip_addr[0]&0xFF && 168 == ip_addr[1]&0xFF) {
			ret = ((ip_addr[0])&0xFF)
				| ((ip_addr[1]&0xFF)<<8)
				| ((ip_addr[2]&0xFF)<<16)
			| ((ip_addr[3]&0xFF)<<24);
			LOGD("IP:%d", ret);
			break;
		}

		if (ret != 0) break;
		ifr++;
	}

	return ret;
}

jint getHotspotIp (JNIEnv* env, jobject thiz, jstring node){
    int fd = -1;
    struct ifreq* ifr;
    struct ifconf ifc;
    int numreqs = 10;
    int n, error = -1;
    char* ip_addr = NULL;
    int try_count = 5;
    jint ret = 0;


    ifc.ifc_buf = NULL;


#if 0
    if (node != NULL){
        prefix = (*env)->GetStringUTFChars(node, NULL);
        if (prefix == NULL){
            goto out;
        }
        prefix_len = strlen(prefix);
    }
#endif

    fd = socket(AF_INET, SOCK_DGRAM, 0);
    if (fd < 0){
        LOGE("Error: no inet socket available");
//        printf("Error: no inet socket available");
        goto out;
    }

    while (try_count >= 0) {
    	LOGD("try_count:%d", try_count);
        ifc.ifc_len = sizeof(struct ifreq) * numreqs;
        ifc.ifc_buf = (char*)realloc(ifc.ifc_buf, ifc.ifc_len);

        if (ioctl(fd, SIOCGIFCONF, &ifc) < 0) {
            goto out;
        }
        if (ifc.ifc_len == (int)(sizeof(struct ifreq) * numreqs)) {
            // assume it overflowed and try again
            numreqs += 10;
            continue;
        }

        ifr = ifc.ifc_req;
        for (n = 0; n < ifc.ifc_len; n += sizeof(struct ifreq)) {
        	int index = 0;
        	const char* prefix = NULL;
        	int prefix_len = 0;
        	for(; index < NELEM(patterns); index++){
        		prefix = patterns[index].name;
        		prefix_len = patterns[index].len;

				if (!strncmp(ifr->ifr_name, prefix, prefix_len)){
					ip_addr = (char*)(&((struct sockaddr_in *)&ifr->ifr_addr)->sin_addr);
					LOGE("IPIPIP %d.%d.%d.%d\n", ip_addr[3], ip_addr[2], ip_addr[1], ip_addr[0]);
					ret = ((ip_addr[0])&0xFF)
						| ((ip_addr[1]&0xFF)<<8)
						| ((ip_addr[2]&0xFF)<<16)
						| ((ip_addr[3]&0xFF)<<24);
					LOGD("%s: %d.%d.%d.%d, %d\n", ifr->ifr_name ? ifr->ifr_name : "NO Name",
							ip_addr[0],
							ip_addr[1],
							ip_addr[2],
							ip_addr[3],
							ret);
                	break;
            	}
        	}

        	if (ret != 0) break;
            ifr++;

        }

        if (ret != 0) {
			break;
		} else if (0 == try_count){ // Not found, but try timeout.
			// Try to find a private network address of C type.
			ret = getPrivateCTypeNetwork(ifc);
			break;
        }

        try_count--;
        usleep(1000);
    }
out:
    if (fd != -1){
        close(fd);
    }

    if (ifc.ifc_buf != NULL){
        free(ifc.ifc_buf);
    }
    return ret;//ip_addr;
}

static JNINativeMethod nativeMethods[] = {
    {"native_getHotspotIp", "(Ljava/lang/String;)I",
            (void*)getHotspotIp}
};

static int registerNativeMethods_GetIP(JNIEnv* env) {
    int result = -1;
    jclass clazz = env->FindClass("com/ivy/appshare/engin/connection/implement/ConnectionManagement");

    if (NULL != clazz) {
        if (env->RegisterNatives(clazz, nativeMethods, sizeof(nativeMethods)
                / sizeof(nativeMethods[0])) == JNI_OK) {
            result = 0;
        }
    }

    return result;
}

int registerNativeMethods_Encode(JNIEnv* env);

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env = NULL;
    jint result = -1;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return result;
    }

    if (env == NULL) {
        return result;
    }
    if (registerNativeMethods_GetIP(env) != 0) {
        return result;
    }

    if (registerNativeMethods_Encode(env) != 0) {
        return result;
    }

    result = JNI_VERSION_1_4;
    return result;
}



int main(){
    getHotspotIp(NULL, NULL, NULL);
    return 1;
}
