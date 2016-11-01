#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include <cpu-features.h>

jstring Java_net_jbong_libffmpeg_ArmArchHelper_cpuArchFromJNI(JNIEnv* env, jobject obj){
	char arch_info[11] = "";

	// checking if CPU is of ARM family or not
		if (android_getCpuFamily() == ANDROID_CPU_FAMILY_ARM) {
			strcpy(arch_info, "ARM");

			// checking if CPU is ARM v7 or not
			uint64_t cpuFeatures = android_getCpuFeatures();
			if ((cpuFeatures & ANDROID_CPU_ARM_FEATURE_ARMv7) != 0) {
			    strcat(arch_info, " v7");

				// checking if CPU is ARM v7 Neon
				if((cpuFeatures & ANDROID_CPU_ARM_FEATURE_NEON) != 0) {
				    strcat(arch_info, "-neon");
				}
			}
		}
		return (*env)->NewStringUTF(env, arch_info);
}