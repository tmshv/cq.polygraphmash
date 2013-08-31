public interface ITool{
	String getName();
	
	void update(PVector coord);
	void on(PVector coord);
	void off(PVector coord);
}