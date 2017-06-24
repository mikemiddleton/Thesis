package helpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.Session;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import utilities.SegaLogger;

import com.rbnb.sapi.SAPIException;

import edu.nau.rtisnl.SegaWebException;

@ApplicationScoped
public class PersistentSinkSessionHandler{
	private final HashSet<Session> sessions;
	private final HashSet<DataFetchHelper> sinks;

	private SegaLogger log;

	public PersistentSinkSessionHandler(){
		sessions = new HashSet<Session>();
		sinks = new HashSet<DataFetchHelper>();
	}

	public void addSession(Session session) {

		try {
			log = new SegaLogger("/usr/share/tomcat7/segalogs/PersistentSinkSessionHandler.txt");
		} catch (IOException e) {
			System.out.println("ERROR: Cannot create logger /usr/share/tomcat7/segalogs/SegaDataRequestServlet.txt");
			e.printStackTrace();
		}
		sessions.add(session);
		log.write("Session added");
	}

	public void removeSession(Session session) {
		sessions.remove(session);
		for(DataFetchHelper dfh : sinks){
			if(dfh.getClientName().equals(Integer.toString(session.hashCode()))){
				dfh.disconnect();
				sinks.remove(dfh);
			}
		}
		log.write("Session removed");
	}


	public void addSink(DataFetchHelper dataHelper, Session session) {
		dataHelper.connect(Integer.toString(session.hashCode()));
		sinks.add(dataHelper);
		log.write("Added");
	}

	public DataFetchHelper getSinkBySession(Session session){
		for(DataFetchHelper dfh : sinks){
			if(dfh.getClientName().equals(Integer.toString(session.hashCode()))){
				return dfh;
			}
		}
		return null;
	}

	public void poll(final Session session, final String update_data_interval, String update_server_ip, String update_channel_list, String update_output_style){
		
		final String [] parsedChannels = update_channel_list.split(",");

		final DataFetchHelper dataHelper;
		if ((dataHelper = getSinkBySession(session)) == null){
			return;
		}


		ArrayList<RBNBChannelObject> rbnbData = null;
		int interval = Integer.parseInt(update_data_interval);

		try {
			rbnbData = dataHelper.getData(parsedChannels, (System.currentTimeMillis() / 1000 - interval), interval, "absolute");
		} catch (SegaWebException e) {
			log.write(e);
		} catch (SAPIException e) {
			log.write(e);
		}
		if (rbnbData != null) {
			if (rbnbData instanceof ArrayList<?>) {
				sendToSession(session, update_output_style.equals("Plotting Utility") ? getReducedJSONString(rbnbData) : getJSONString(rbnbData));
			}
		}							


	}

	private void sendToSession(Session session, String message) {
		try {
			session.getBasicRemote().sendText(message);
		} catch (IOException e) {
			log.write(e);
		}
	}

	@SuppressWarnings("unchecked")
	public String getJSONString(ArrayList<RBNBChannelObject> rbco) {
		JSONArray channelObjs = new JSONArray();
		for (RBNBChannelObject co : rbco) {
			JSONObject channel = new JSONObject();
			channel.put("channel_name", co.getChannel_name());
			channel.put("sample_type_ID", co.getSample_type_ID());
			channel.put("sample_type_name", co.getSample_type_name());
			JSONArray xyzData = new JSONArray();
			List<SampleTimestampPackage> xyzs = co.getSample_data();
			Collections.sort(xyzs);
			
			for (SampleTimestampPackage xyz : xyzs) {
				JSONObject dp = new JSONObject();
				dp.put("rbnb_timestamp", xyz.getRbnb_timestamp());
				dp.put("sample_timestamp", xyz.getSample_timestamp());
				if (co.getSample_type_ID() == 10) {
					byte[] dtArr = (byte[]) xyz.getSample_data();
					JSONArray bArr = new JSONArray();
					for (int i = 0; i < dtArr.length; i++) {
						bArr.add(dtArr[i]);
					}
					dp.put("sample_data", bArr);
				} else {
					dp.put("sample_data", xyz.getSample_data());
				}

				xyzData.add(dp);
			}
			channel.put("xyzData", xyzData);
			channelObjs.add(channel);
		}
		return channelObjs.toJSONString();
	}	
	
	@SuppressWarnings("unchecked")
	public String getReducedJSONString(ArrayList<RBNBChannelObject> rbco){
			JSONArray channelObjs = new JSONArray();
			for (RBNBChannelObject co : rbco) {
				JSONObject channel = new JSONObject();
				channel.put("channel_name", co.getChannel_name());
				channel.put("sample_type_ID", co.getSample_type_ID());
				channel.put("sample_type_name", co.getSample_type_name());
				JSONArray xyzData = new JSONArray();
				List<SampleTimestampPackage> xyzs = co.getSample_data();
				Collections.sort(xyzs);
				
				for (SampleTimestampPackage xyz : xyzs) {
					ArrayList <Object> dp = new ArrayList<Object>();
					dp.add(xyz.getSample_timestamp());
					if (co.getSample_type_ID() == 10) {
						byte[] dtArr = (byte[]) xyz.getSample_data();
						ArrayList<Object> bArr = new JSONArray();
						for (int i = 0; i < dtArr.length; i++) {
							bArr.add(dtArr[i]);
						}
						dp.add(bArr);
					} else {
						dp.add(xyz.getSample_data());
					}
					xyzData.add(dp);
				}
				channel.put("xyzData", xyzData);
				channelObjs.add(channel);
			}
			return channelObjs.toJSONString();
	}



}
