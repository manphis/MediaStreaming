package work.eason.streaminglibrary;

public class AudioParameters {
	public String host_ipaddr;
    public int host_port;
    public int listen_port;

    public int codec_payload_number;
    public String codec_payload_name;
    public int codec_sample_rate;
    public int codec_bit_rate;

    public int rtcp_pli_ctrl;
    public int rtcp_fir_ctrl;
    public int rtcp_ctrl;

    public int fec_ctrl;                   // 0:disable, 1:enable (get it from provision file for enabling FEC or not)
    public int fec_sendPayloadType;        // from opposite SDP. set '-1' if opposite doesn't support FEC
    public int fec_receivePayloadType;     // from self SDP

    public int srtp_ctrl;

    public int dtmf_pt;
    public int dtmf_transmit_mode;

    public int tos;
    
    public AudioParameters(MediaParameters mediaParam) {
    	host_ipaddr = "127.0.0.1";
        host_port = 7777;
        listen_port = 7777;

        codec_payload_number = 111;		//8
        codec_payload_name = "OPUS";	//PCMU PCMA OPUS);
        codec_sample_rate = 16000;		//8000;
        codec_bit_rate = 24000;		//0;

        rtcp_pli_ctrl = 0;
        rtcp_fir_ctrl = 0;
        rtcp_ctrl = 1;

        fec_ctrl = 1;
        fec_sendPayloadType = 98;		//110;
        fec_receivePayloadType  = 98;	//110;

        srtp_ctrl = 0;

        dtmf_pt = 101;
        dtmf_transmit_mode = 2;		//1;

        tos = 0;

    }
}