public class SpaceFunction{
	public String name;
	public ArrayList<SpaceUser> users;
	public int c;

	public SpaceFunction (String name, int c) {
		this.name = name;
		this.c = c;
		this.users = new ArrayList<SpaceUser>();
	}

	public int calcPower(){
		int s = users.size();
		return s == 0 ? 1 : s;
		// int total = 0;
		// for(SpaceUser su : users){
		// 	total += su.power;
		// }
		// return total;
	}
}