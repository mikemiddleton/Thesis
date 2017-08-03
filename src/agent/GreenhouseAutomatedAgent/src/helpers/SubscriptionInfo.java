package helpers;
/*
 * Objects fo this type will possess the connection information necessary
 * to make a new mqtt subscription client
 */
public class SubscriptionInfo{
	public String subTopic;
	public String broker;
	public String gsCommonName;
	public int qos;
	
	public SubscriptionInfo(String subTopic, String broker, String gsCommonName, int qos){
		this.subTopic = subTopic;
		this.broker = broker;
		this.gsCommonName = gsCommonName;
		this.qos = qos;
	}
}