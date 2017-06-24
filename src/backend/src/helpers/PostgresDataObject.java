package helpers;

import java.util.ArrayList;
import java.util.List;

public class PostgresDataObject {

	private String rbnb_channel_name = null;
	private String wisard_serial_id = null;
	private String sp_serial_id = null;
	private String transducer_serial_id = null;
	private String stream_id = null;
	private String splocation = null;
	
	
	private List<KeyValueObject> sample_data = new ArrayList<KeyValueObject>();
	
	
	public PostgresDataObject(String rbnb_channel_name,
							String wisard_serial_id,
							String sp_serial_id,
							String transducer_serial_id,
							String stream_id,
							String splocation){
		setRbnb_channel_name(rbnb_channel_name);
		setWisard_serial_id(wisard_serial_id);
		setSp_serial_id(sp_serial_id);
		setTransducer_serial_id(transducer_serial_id);
		setStream_id(stream_id);
		setSplocation(splocation);
	}
	


	/**
	 * @return the rbnb_channel_name
	 */
	public String getRbnb_channel_name() {
		return rbnb_channel_name;
	}



	/**
	 * @param rbnb_channel_name the rbnb_channel_name to set
	 */
	public void setRbnb_channel_name(String rbnb_channel_name) {
		this.rbnb_channel_name = rbnb_channel_name;
	}



	/**
	 * @return the wisard_serial_id
	 */
	public String getWisard_serial_id() {
		return wisard_serial_id;
	}


	/**
	 * @param wisard_serial_id the wisard_serial_id to set
	 */
	public void setWisard_serial_id(String wisard_serial_id) {
		this.wisard_serial_id = wisard_serial_id;
	}


	/**
	 * @return the sp_serial_id
	 */
	public String getSp_serial_id() {
		return sp_serial_id;
	}


	/**
	 * @param sp_serial_id the sp_serial_id to set
	 */
	public void setSp_serial_id(String sp_serial_id) {
		this.sp_serial_id = sp_serial_id;
	}


	/**
	 * @return the transducer_serial_id
	 */
	public String getTransducer_serial_id() {
		return transducer_serial_id;
	}


	/**
	 * @param transducer_serial_id the transducer_serial_id to set
	 */
	public void setTransducer_serial_id(String transducer_serial_id) {
		this.transducer_serial_id = transducer_serial_id;
	}



	/**
	 * @return the sample_data
	 */
	public List<KeyValueObject> getSample_data() {
		return sample_data;
	}


	/**
	 * @param sample_data the sample_data to set
	 */
	public void setSample_data(List<KeyValueObject> sample_data) {
		this.sample_data = sample_data;
	}


	/**
	 * @return the stream_id
	 */
	public String getStream_id() {
		return stream_id;
	}


	/**
	 * @param stream_id the stream_id to set
	 */
	public void setStream_id(String stream_id) {
		this.stream_id = stream_id;
	}


	/**
	 * @return the splocation
	 */
	public String getSplocation() {
		return splocation;
	}


	/**
	 * @param splocation the splocation to set
	 */
	public void setSplocation(String splocation) {
		this.splocation = splocation;
	}


	
	
	
	
	
	
}
