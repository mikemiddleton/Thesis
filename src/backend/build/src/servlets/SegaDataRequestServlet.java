package servlets;

import helpers.DataFetchHelper;
import helpers.KeyValueObject;
import helpers.PostgresDataObject;
import helpers.PostgresFetchHelper;
import helpers.RBNBChannelObject;
import helpers.SampleTimestampPackage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import utilities.ConnectionHandler;
import utilities.ObjectSizeCalculator;
import utilities.SegaLogger;
import edu.nau.rtisnl.SegaWebException;


/**
 * Servlet implementation class SegaDataRequestServlet
 */
@WebServlet("/SegaDataRequestServlet")
public class SegaDataRequestServlet extends HttpServlet {
	/** Required by java.io.Serializable */
	private static final long serialVersionUID = -1729542503675338717L;
	/** Log file and location to write to disk */
	private static SegaLogger log;
	/** Date format from the web portal's final data request page */
	private static SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d H:m:s z y");
	/** Date format of the IP camera image files */
	private static SimpleDateFormat sdf_ipcam = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SegaDataRequestServlet() {
		super();
		try {
			log = new SegaLogger("/usr/share/tomcat7/segalogs/SegaDataRequestServlet.txt");
		} catch (IOException e) {
			System.out.println("ERROR: Cannot create logger /usr/share/tomcat7/segalogs/SegaDataRequestServlet.txt");
			e.printStackTrace();
		}

	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */

	@Override
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//Fetch the servlet context path
		String servletContextPath = getServletContext().getRealPath(File.separator);

		try {
			if (request.getSession().getAttribute("dataUpdate") != null) {
				if (request.getSession().getAttribute("dataUpdate") instanceof ArrayList<?>) {
					ArrayList<RBNBChannelObject> data = (ArrayList<RBNBChannelObject>) request.getSession().getAttribute("dataUpdate");
					PrintWriter out = response.getWriter();
					out.write(getJSONString(data));
					out.flush();
					out.close();
				}

			} else if (request.getSession().getAttribute("rbnbData") != null) {
				
				if (request.getSession().getAttribute("rbnbData") instanceof ArrayList<?>) {
					ArrayList<RBNBChannelObject> data = (ArrayList<RBNBChannelObject>) request.getSession().getAttribute("rbnbData");

					if (response.getContentType().contains("text/csv") ||
							response.getContentType().contains("application/vnd.ms-excel")) {
						PrintWriter out = response.getWriter();
						out.println("Request Parameter Name, Request Parameter Value");
						Enumeration<String> attrs = request.getSession().getAttributeNames();
						String attrName = "";
						for (; attrs.hasMoreElements();) {
							attrName = attrs.nextElement();
							if (attrName.contains("selected")) {
								out.println(attrName + "," + request.getSession().getAttribute(attrName));
							}
						}
						out.write("\n\n\n");
						out.println("Column Name, Column Units");
						out.println("[Channel Name]:, String");
						out.println("[RBNB Timestamp]:, Long - milliseconds since 1970");
						out.println("[Sample Time]:, Long - milliseconds since 1970");
						out.println("[Sample Data]:, String");
						out.println("[Sample Time]:, String");
						out.println("[Sample Date String]:, String");
						out.println("[Value]:, Specified by 'Format'");
						out.println("[Format]:, String");
						out.write("\n\n\n");
						out.println("Channel Name,RBNB Timestamp,Sample Timestamp,Sample Date,Sample Time,Sample Date String,Value,Format");
						for (RBNBChannelObject c : data) {
							for (SampleTimestampPackage xyz : c.getSample_data()) {
								out.write(c.getChannel_name() + ",");
								out.write(xyz.getRbnb_timestamp() + ",");
								out.write(xyz.getSample_timestamp() + ",");
								Date d = new Date(xyz.getSample_timestamp());
								SimpleDateFormat sdf = new SimpleDateFormat("M/d/Y");
								out.write(sdf.format(d) + ",");
								sdf = new SimpleDateFormat("H:mm:ss:SSS");
								out.write(sdf.format(d) + ",");
								sdf = new SimpleDateFormat("EEE MMM d H:mm:ss z y");
								out.write(sdf.format(d) + ",");

								if (c.getSample_type_ID() == 10) {
									out.write("[");
									byte[] tempByteArr = (byte[]) xyz.getSample_data();
									for (int i = 0; i < tempByteArr.length; i++) {
										int onum = tempByteArr[i];
										int num = (onum < 0 ? 0xFF + onum + 1 : onum);
										out.write((num < 16 ? "0x0" : "0x") + Integer.toHexString(num).toUpperCase());
										if (i != tempByteArr.length - 1)
											out.write(" ");
									}
									out.write("],");
								} else {
									out.write(xyz.getSample_data() + ",");

								}
								out.write(c.getSample_type_name() + "\r\n");
								out.flush();
							}
						}

						out.close();
					} 

				}

			} else if (request.getSession().getAttribute("ipcamData") != null && 
					request.getSession().getAttribute("ipcamData") instanceof ArrayList<?>) {
				ArrayList<File> validImages = (ArrayList<File>) request.getSession().getAttribute("ipcamData");
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ZipOutputStream zos = new ZipOutputStream(baos);
				String relative_path;
				
				byte[] bytes;
				for(File f : validImages){
					relative_path = f.getAbsolutePath().replace(servletContextPath, "");
					relative_path = relative_path.replace(SegaDataRequestFormServlet.ipcam_folder,"");
					//Create Zip entry with Location/Camera_ID/Image
					zos.putNextEntry(new ZipEntry(relative_path));
					bytes = Files.readAllBytes(f.toPath());
					zos.write(bytes,0,bytes.length);	
					zos.closeEntry();
				}

				zos.flush();
				baos.flush();
				zos.close();
				baos.close();

				ServletOutputStream sos = response.getOutputStream();				
				sos.write(baos.toByteArray());
				sos.flush();
				sos.close();





			}


		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			log.write(errors);
			request.getSession().setAttribute("error_msg", e.getMessage());
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		//Fetch the servlet context path
		String servletContextPath = getServletContext().getRealPath(File.separator);
		
		String selectedServerAddrName, selectedChannels, selectedDataIntervalName, selectedStartDate, selectedEndDate, 
		selectedTimeIntervalValue, selectedOutputStyle, selectedDataInterval, selectedDataType;
		selectedServerAddrName = selectedChannels = selectedDataIntervalName = selectedStartDate = selectedEndDate = 
				selectedTimeIntervalValue = selectedOutputStyle = selectedDataInterval = selectedDataType = null;

		//String update_data_interval, update_server_ip, update_channel_list, update_output_style;
		//update_data_interval = update_server_ip = update_channel_list = update_output_style = null;

		String[] parsedChannels = null;
		String redirect = null;
		ArrayList<RBNBChannelObject> rbnbData = null;
		ArrayList<PostgresDataObject> psqlData = null;
		String continueSubmit = null;
		request.getSession().removeAttribute("error_msg");
		try {

			if((redirect = request.getParameter("redirect")) == null){
				throw new SegaWebException(SegaWebException.error_type.REDIRECT);
			}
			if((continueSubmit = request.getParameter("continue_submit")) == null){
				continueSubmit = "False";
			}
			if((selectedOutputStyle = request.getParameter("selectedOutputStyle")) == null && request.getParameter("selectedDataInterval") != null){
				throw new SegaWebException(SegaWebException.error_type.OUTPUT_STYLE);
			}


			if ((selectedServerAddrName = request.getParameter("selectedServerAddrName")) != null) {
				request.getSession().removeAttribute("rbnbData");
				request.getSession().removeAttribute("psqlData");
				request.getSession().removeAttribute("ipcamData");


				if ((selectedChannels = request.getParameter("selectedChannels")) != null) {

					parsedChannels = selectedChannels.split(",");

					if ((selectedDataType = request.getParameter("selectedDataType")) != null) {

						if ((selectedOutputStyle = request.getParameter("selectedOutputStyle")) != null) {
							if(selectedDataType.equalsIgnoreCase("camera_images")){
								ArrayList<File> validImages = new ArrayList<File>();

								File ipcam_dir;
								String filename;
								Date startDate = null, endDate = null, tempDate = null;
								if ((selectedDataInterval = request.getParameter("selectedDataInterval")) != null) {
									//Case '0' is 'Date Range' and case '1' is 'Date and Time'
									if (selectedDataInterval.equals("0") || selectedDataInterval.equals("1")) {
										if ((selectedStartDate = request.getParameter("selectedStartDate")) != null) {
											if ((selectedEndDate = request.getParameter("selectedEndDate")) != null) {
												startDate = sdf.parse(selectedStartDate);
												endDate = sdf.parse(selectedEndDate);

												for(String s : parsedChannels){

													ipcam_dir = new File(servletContextPath + SegaDataRequestFormServlet.ipcam_folder + s);
													File[] files = ipcam_dir.listFiles();
													if(files != null){
														for(File f : files){
															if(f.isFile()){
																//Trim by the file extension
																filename = f.getName().substring(0,f.getName().indexOf("."));

																tempDate = sdf_ipcam.parse(filename);

																if(tempDate.getTime() > startDate.getTime() && tempDate.getTime() < endDate.getTime()){
																	validImages.add(f);
																}

															}
														}
													} else {
														throw new SegaWebException(SegaWebException.error_type.NO_DATA);
													}
												}

											}
										}
									}
									//Case '2' is 'Time Interval'
									else if (selectedDataInterval.equals("2")) {
										if ((selectedDataIntervalName = request.getParameter("selectedDataIntervalName")) != null) {
											if ((selectedTimeIntervalValue = request.getParameter("selectedTimeIntervalValue")) != null) {

												long goBackInTime = Long.parseLong(selectedTimeIntervalValue)*1000;

												endDate = new Date();
												startDate = new Date(endDate.getTime() - goBackInTime);

												for(String s : parsedChannels){

													ipcam_dir = new File(servletContextPath + SegaDataRequestFormServlet.ipcam_folder + s);
													File[] files = ipcam_dir.listFiles();
													if(files != null){
														for(File f : files){
															if(f.isFile()){
																//Trim by the file extension
																filename = f.getName().substring(0,f.getName().indexOf("."));

																tempDate = sdf_ipcam.parse(filename);

																if(tempDate.getTime() > startDate.getTime() && tempDate.getTime() < endDate.getTime()){
																	validImages.add(f);
																}

															}
														}
													} else {
														throw new SegaWebException(SegaWebException.error_type.NO_DATA);
													}
												}
											}
										}

									}
								}


								if(!validImages.isEmpty()){
									//Sort images by date
									Collections.sort(validImages, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);

									if(selectedOutputStyle.equalsIgnoreCase("View Gallery")){
										//Build a list of URL objects
										ArrayList<KeyValueObject> imageURLs = new ArrayList<KeyValueObject>();
										String relative_path,camera_id;
										for(File f : validImages){
											relative_path = f.getAbsolutePath().replace(servletContextPath, "");
											camera_id = relative_path.replace(SegaDataRequestFormServlet.ipcam_folder,"");
											camera_id = camera_id.substring(0,camera_id.lastIndexOf("/"));
											
											imageURLs.add(new KeyValueObject(relative_path,
													camera_id + " - " + sdf_ipcam.parse(f.getName().substring(0,f.getName().indexOf(".")))));
										}

										request.getSession().setAttribute("imageURLs", imageURLs);
										redirect = "/segaWeb/data/gallery/gallery.jsp";

									}else if(selectedOutputStyle.equalsIgnoreCase("Download ZIP")){
										request.getSession().setAttribute("ipcamData", validImages);
										response.setContentType("application/zip");
										response.setHeader("Content-disposition",
												"attachment;filename=ipcam_images_" + 
														new SimpleDateFormat("YYYY-MM-dd_H-mm-ss").format(new Date(System.currentTimeMillis())) 
														+ ".zip;");
										//response.setCharacterEncoding("UTF-8");
										doGet(request, response);

									}
								}else {
									throw new SegaWebException(SegaWebException.error_type.NO_DATA);
								}

							}else{
								if (selectedOutputStyle.equals("Dashboard Style")) {
									if (selectedDataType.equalsIgnoreCase("rbnb_data")) {
										DataFetchHelper dataHelper = new DataFetchHelper(selectedServerAddrName);
										// If we are downloading a CSV double the fetch timeout
										if (continueSubmit.equals("True")){
											dataHelper.setTimeout(6000);
										}
										dataHelper.connect("TempDataRequestSink");
										rbnbData = dataHelper.getData(parsedChannels, 0., 0., "newest");
										dataHelper.disconnect();
										if (rbnbData != null) {
											request.getSession().setAttribute("rbnbData", rbnbData);
										}
									} else if (selectedDataType.equalsIgnoreCase("archived_sega_data")) {
										ConnectionHandler connector = new ConnectionHandler(log);
										if (connector.connect("/opt/RBNB/processors/client_configs/PostgresHandler/db_connect")) {
											PostgresFetchHelper pgh = new PostgresFetchHelper(connector, log);
											if (continueSubmit.equals("True")){
												pgh.setTimeout(6000);
											}
											psqlData = pgh.getDashboardData(parsedChannels);
											if (psqlData != null) {
												request.getSession().setAttribute("psqlData", psqlData);
											} else {
												log.write("No Postgres Data Found");
											}
											connector.disconnect();
										}

									}
								} else {
									if ((selectedDataInterval = request.getParameter("selectedDataInterval")) != null) {
										//Case '0' is 'Date Range' and case '1' is 'Date and Time'
										if (selectedDataInterval.equals("0") || selectedDataInterval.equals("1")) {

											if ((selectedStartDate = request.getParameter("selectedStartDate")) != null) {
												if ((selectedEndDate = request.getParameter("selectedEndDate")) != null) {
													if (selectedDataType.equalsIgnoreCase("rbnb_data")) {
														DataFetchHelper dataHelper = new DataFetchHelper(selectedServerAddrName);
														// If we are downloading a CSV double the fetch timeout
														if (selectedOutputStyle.equals("Download as CSV") || continueSubmit.equals("True")){
															dataHelper.setTimeout(6000);
														}
														dataHelper.connect("TempDataRequestSink");
														double start = sdf.parse(selectedStartDate).getTime() / 1000.;
														double end = sdf.parse(selectedEndDate).getTime() / 1000.;
														double duration = end - start;
														rbnbData = dataHelper.getData(parsedChannels, start, duration, "absolute");
														dataHelper.disconnect();
														if (rbnbData != null) {
															request.getSession().setAttribute("rbnbDataJSON", getReducedJSONString(rbnbData));
														} else {
															request.getSession().removeAttribute("rbnbDataJSON");
														}
													} else if (selectedDataType.equalsIgnoreCase("archived_sega_data")) {
														ConnectionHandler connector = new ConnectionHandler(log);
														if (connector.connect("/opt/RBNB/processors/client_configs/PostgresHandler/db_connect")) {
															PostgresFetchHelper pgh = new PostgresFetchHelper(connector, log);
															if (selectedOutputStyle.equals("Download as CSV") || continueSubmit.equals("True")){
																pgh.setTimeout(6000);
															}
															psqlData = pgh.getData(parsedChannels, sdf.parse(selectedStartDate),
																	sdf.parse(selectedEndDate));
															if (psqlData != null) {
																request.getSession().setAttribute("psqlDataJSON", psqlData);
															} else {
																request.getSession().removeAttribute("psqlData");
															}
															connector.disconnect();
														}

													}
												}
											}

										}

										//Case '2' is 'Time Interval'
										else if (selectedDataInterval.equals("2")) {
											if ((selectedDataIntervalName = request.getParameter("selectedDataIntervalName")) != null) {
												if ((selectedTimeIntervalValue = request.getParameter("selectedTimeIntervalValue")) != null) {
													if (selectedDataType.equalsIgnoreCase("rbnb_data")) {
														DataFetchHelper dataHelper = new DataFetchHelper(selectedServerAddrName);
														// If we are downloading a CSV double the fetch timeout
														if (selectedOutputStyle.equals("Download as CSV") || continueSubmit.equals("True")){
															dataHelper.setTimeout(6000);
														}
														dataHelper.connect("TempDataRequestSink");
														if (selectedDataIntervalName.equals("From Time Now")) {
															Double duration = Double.parseDouble(selectedTimeIntervalValue);
															rbnbData = dataHelper.getData(parsedChannels,
																	(System.currentTimeMillis() / 1000.0 - duration), duration, "absolute");
														} else if (selectedDataIntervalName.equals("From Data Point")) {
															rbnbData = dataHelper.getData(parsedChannels, 0,
																	Double.parseDouble(selectedTimeIntervalValue), "newest");
														}

														dataHelper.disconnect();
														if (rbnbData != null) {
															request.getSession().setAttribute("rbnbDataJSON", getReducedJSONString(rbnbData));
														} else {
															request.getSession().removeAttribute("rbnbDataJSON");
														}

													} else if (selectedDataType.equalsIgnoreCase("archived_sega_data")) {
														//TODO:

													}
												}
											}
										}
									}
								}

							}
						}
					}

				}

			}

			if ((selectedOutputStyle = request.getParameter("selectedOutputStyle")) != null) {
				if (selectedOutputStyle.equals("Download as CSV") || selectedOutputStyle.equals("Download as Excel")) {
					
					if ((rbnbData != null) || (psqlData != null)) {
						response.setContentType("text/csv");
						SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd_H-mm-ss");
						response.setHeader("Content-disposition",
								"attachment;filename=segadata_" + sdf.format(new Date(System.currentTimeMillis())) + ".csv;");
						response.setCharacterEncoding("UTF-8");
						
						request.getSession().setAttribute("rbnbData", rbnbData);
						
						doGet(request, response);
					} else {
						throw new SegaWebException(SegaWebException.error_type.NO_DATA);
					}
				}
			}



		}catch (SegaWebException e){
			// Add the error_msg attribute the redirect from the SegaWebException error.
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			log.write(errors);
			request.getSession().setAttribute("error_msg", e.getMessage());
			redirect = "/segaWeb/data/form/datarequest.jsp";
		}catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			log.write(errors);
			request.getSession().setAttribute("error_msg", e.getMessage());
			redirect = "/segaWeb/data/form/datarequest.jsp";
		}

		if (redirect != null) {
			long dataSize = 0;
			if (redirect.endsWith("segaplotting.jsp")){
				try {
					if(rbnbData != null){
						dataSize = ObjectSizeCalculator.sizeOf(rbnbData!=null ? request.getSession().getAttribute("rbnbDataJSON") : psqlData);
						request.getSession().setAttribute("requestDataSize", dataSize);
						if(dataSize > 7000000){
							throw new SegaWebException(SegaWebException.error_type.FETCH_SIZE);
						}
					}
				}
				catch(SegaWebException e){
					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					log.write(errors);
					request.getSession().setAttribute("error_msg", e);
					redirect = "/segaWeb/data/form/datarequest.jsp";
				}catch (IllegalAccessException e) {
					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					log.write(errors);
					request.getSession().setAttribute("error_msg", e);
					redirect = "/segaWeb/data/form/datarequest.jsp";
				}
			}

			if (!(redirect.equals("none"))){
				response.sendRedirect(redirect);
			}
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
