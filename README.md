# FFmpegBasic

잡매칭SW경진대회를 첨부된 ppt와 글을 통해 진행

개발 목적 및 느낀 점 : 기존에 안드로이드에서 비디오를 재생시켜주는 VideoView가 지원하는 파일을 제외하고 HEVC 및 여러 파일을 decoding하고 재생시켜주는 앱이 따로 필요하다고 생각되어 구현을 시작하게 되었습니다. 개발을 진행함에 있어서 ndk설정 및 HM을 이용하는 경험을 가지는 기회를 가졌으며, 여러 파일들의 decoding을 지원해주는 FFmpeg이란 라이브러리를 이용할 수 있게 되었습니다. 서버 클라이언트에서도 채팅 프로그램처럼 string 값들만을 주고 받는 경우가 아니라 더 나아가 구조체를 이용해서 해당하는 byte를 주고 받을 수 있게 되었습니다.
 
0. HM
- 개발 프로그램은 아니지만 HM encoder를 이용해서 raw data로 이루어진 yuv파일을 bin파일로 encoding합니다. 해당 파일을 인코딩하기 위한 configure 파일들을 설정하고 bat파일을 생성하여 실행하면 파일이 Encoding되며, Encoding 되어진 파일이 생성됩니다.
 
bat 파일 ex )
TAppEncoder.exe -c encoder_randomaccess_main.cfg -c BQSquare.cfg -b .\bitstream\BQSquare_416x240_60.bin > .\Log\BQSquare_416x240_60.txt
 
1. Server ( C )
- pthread를 이용하여 여러 Client를 accept할 수 있도록 하였지만, File Data 전송이기 때문에 파일 참조를 여러 쓰레드가 할 경우 정상적으로 참조가 되지 않으므로, 하나의 쓰레드가 끝났을 경우 다른 Client가 File Data 전송을 받을 수 있도록 pthread_join함수를 이용하였습니다. 파일 참조를 하기 위해서 Critical Section을 생성해 mutex를 이용하려 하였으나, 시간적으로나 실제 이론과 다른 부분이 있는 것을 감안해 추후에 수정할 예정입니다. File을 fread함수로 1396 byte씩 읽어 구조체에 Sequence Number(4 byte)와 함께 1400byte를 Client로 전송합니다. 파일을 만약 다 읽어 보냈을 경우 Bandwidth와 Packet Loss Rate를 계산하고, 해당 Client의와의 접속 및 전송 기록을 Log파일에 기록합니다.
 
이용 서버 환경: Windows Sever 2012 , 연구실 서버
 
2. Client ( Android, Java )
- Server로부터 구조체 Data 즉, sequence number와 video data를 받는 과정에 있어서 sequence number를 처리할 때 Little Endian(C)방식에서 Big Endian(Java)방식으로 변환시켜주기 위한 함수(bytetoint)를 생성하여 변환이 올바르게 될 수 있도록 하였으며, video data는 해당 경로에 파일로써 저장되도록 write하였습니다. 저장된 파일을 decoding하는데 있어 java로 구현된 ffmpeg library가 아래 첫 번째 링크에 존재하여 decoding command를 작성해 비디오를 재생하기위한 mp4파일로 디코딩하였습니다. 비디오를 재생하기위한 플레이어에 필요한 라이브러리는 직접 ffmpeg c 라이브러리를 2번째 링크로부터 다운로드받아 jni폴더내에서 c함수를 java로 가져와 쓸 수 있도록 하기 위한 android ndk를 3번째 링크로부터 다운받고 ndk설정 및 build 뒤 플레이 합니다.
 
Client 및 개발 환경 : Galaxy S6, Eclipse
 
1. java ffmpeg decoding lib https://github.com/WritingMinds/ffmpeg-android-java
2. ffmpeg c library site https://ffmpeg.zeranoe.com/builds/
3. android ndk site https://developer.android.com/ndk/downloads/index.html
 
참고사이트
Android NDK FFmpeg compile 강좌( 1 ~ 4 )
http://www.androidpub.com/index.php?mid=android_dev_info&search_target=tag&search_keyword=FFmpeg&document_srl=1645684
C와 Java 구조체 소켓 통신
http://egloos.zum.com/limiteddaily/v/2929500
http://yagi815.tistory.com/999
FFmpeg 이용시 에러처리
http://jnexonsoft.tistory.com/archive/20140203
Little Endian -> Big Endian (bytetoInt)
http://forum.falinux.com/zbxe/index.php?document_srl=569618&mid=lecture_tip
Player 구현 tutorial
http://dranger.com/ffmpeg/
 
참고서적
열혈 TCP/IP 소켓 프로그래밍 - 윤성우
