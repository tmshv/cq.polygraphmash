public class Remap{
	double mny;
	double mxy;
	double r;
	public Remap (double mny, double mxy, double r) {
		this.mny = mny;
		this.mxy = mxy;
		this.r = r;
	}

	PVector remap(PVector coord){
		PVector a = PVector.sub(coord, new PVector(0, (float) mny));
		a.mult((float) r);
		return a;
	}
}