package edu.nau.rtisnl;

/**
 * 
 * @author akr72
 * 
 */
public class SegaWebException extends Exception {

	private static final long serialVersionUID = 3194457105173344397L;
	private String message = null;
	private error_type error = null;

	public static enum error_type {
		FETCH_TIMEOUT("A fetch timeout occurred."), 
		FETCH_SIZE("The calculated data fetch size is too large."),
		REDIRECT("No redirect parameter was set."),
		NO_DATA("There is no data in the request."),
		OUTPUT_STYLE("There is no output style."),
		UNKOWN("An unknown exception has occured."),
		NO_CONNECTION("There is not an RBNB connection running for this set of channels.");

		private final String message;
		error_type(String message) { this.message = message; }
		public String getMessage() { return message; }
	}

	
	// Default constructor
	public SegaWebException(){
		super(error_type.UNKOWN.getMessage());
		this.message = error_type.UNKOWN.getMessage();
		this.error = error_type.UNKOWN;
	}
	
	/**
	 * @param error
	 */
	public SegaWebException(SegaWebException.error_type error){
		super(error.getMessage());
		this.message = error.getMessage();
		this.error = error;
	}
	
	/**
	 * @param cause
	 */
	public SegaWebException(Throwable cause){
		super(cause);
		this.error = SegaWebException.error_type.UNKOWN;
		this.message = cause.getMessage();
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public SegaWebException(SegaWebException.error_type error, Throwable cause, boolean enableSuppression, boolean writableStackTrace){
		super(error.getMessage(), cause, enableSuppression, writableStackTrace);
		this.message = error.getMessage();
		this.error = error;
	}

	// Include a toString method so that when printing out the exception, the error message is printed
	@Override
	public String toString() {
		return message;
	}

	@Override
	public String getMessage() {
		return message;
	}
	
	public error_type getError(){
		return error;
	}
}
