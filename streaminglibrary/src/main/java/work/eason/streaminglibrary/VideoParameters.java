package work.eason.streaminglibrary;

public class VideoParameters {
	public String host_ipaddr;
	public int host_port;
	public int listen_port;

	public int codec_payload_type;
	public String codec_payload_name;
	public int codec_sample_rate;

	public int width;
	public int height;
	public int fps;

	public int h264_gop;
	public int h264_rate_ctrl;
	public int h264_max_kbit_rate;
	public int h264_min_kbit_rate;
	public int h264_idr_request_ctrl;
	public int h264_gop_mode;    

	public int rtcp_pli_ctrl;
	public int rtcp_fir_ctrl;
	public int rtcp_ctrl;

	public int fec_ctrl;                   // 0:disable, 1:enable (get it from provision file for enabling FEC or not)
	public int fec_sendPayloadType;        // from opposite SDP. set '-1' if opposite doesn't support FEC
	public int fec_receivePayloadType;     // from self SDP

	public int srtp_ctrl;

	public int tos;
    
	public VideoParameters(MediaParameters mediaParam) {
	    host_ipaddr = "127.0.0.1";
	    host_port = 8888;
	    listen_port = 8888;

	    codec_payload_type = 109;
	    codec_payload_name = "H264";
	    codec_sample_rate = 90000;

	    width = 1280;		//640;
	    height = 720;		//480;
	    fps = 15;

	    h264_gop = fps*5;
		h264_rate_ctrl = 1;
		h264_max_kbit_rate = 2000;
		h264_min_kbit_rate = 50;
		h264_idr_request_ctrl = 1;
		h264_gop_mode = 0;

		rtcp_pli_ctrl = 1;
		rtcp_fir_ctrl = 1;
		rtcp_ctrl = 1;

		fec_ctrl = 1;
	    fec_sendPayloadType = 121;		//111;
	    fec_receivePayloadType = 121;	//111;

		srtp_ctrl = 0;

		tos = 0;
	}
}