package core;
/**
 * Class that stores all the information for a particular user
 * to populate the UI and for use in creating the graph
 * @author Kartikeya
 *
 */
public class IdDescription {
	public String userId;
	public String description;
	public String amount;
	public String date;
	public String short_description;
	
	public IdDescription(String userId, String description, String amount, String date, String short_description) {
		this.userId = userId;
		this.description = description;
		this.amount = amount;
		this.date = date;
		this.short_description = short_description;
	}
}
