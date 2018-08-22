package work.eason.streaminglibrary;

public class MediaParameters {

	public AudioParameters audioPara;
	public VideoParameters videoPara;
	
	public MediaParameters() {
	    audioPara = new AudioParameters(this);
	    videoPara = new VideoParameters(this);
	}
}