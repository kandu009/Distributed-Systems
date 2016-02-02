package distributed_banking;


/**
 * An implementation of {@link IRequest} which is used to halt the entire system
 * 
 * @author rkandur
 *
 */
public class HaltRequestB extends AbstractRequest {

	private static final long serialVersionUID = 1L;
	
	private long startTime_;

	public HaltRequestB() {
		startTime_ = System.currentTimeMillis();
	}
	
	@Override
	public RequestResponse execute() {
		System.out.println("Halting the server process !!!");
		return new RequestResponse(Boolean.TRUE, new String("SUCCESS"), new String("SUCCESS"));
	}
	
	public long getStartTime() {
		return startTime_;
	}
	
	@Override
	public String arguments() {
		return new String();
	}

}
