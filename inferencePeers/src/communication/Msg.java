package communication;


import java.io.Serializable;
public class Msg<MContent extends Serializable> implements Serializable  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected String _sender;

	protected String _recipient;

	protected int _type;

	protected   MContent _content;

	public  Msg(String sender, String recipient, int type,
			MContent content) {
		_sender = sender;
		_recipient = recipient;
		_type = type;
		_content = content;
		
	}
	
	

	public String getRecipient() {
		return _recipient;
	}

	public String getSender() {
		return _sender;
	}

	public int getType() {
		return _type;
	}

	public MContent getContent() {
		return _content;
	}
	
	
	public String toString(){
		return String.valueOf(_type) ;
	}
	

}

