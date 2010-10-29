package org.opensha.sra.calc.portfolioLEC;

public class AssetRuptureResult {
	
	private double mIML;
	private double mLnIML;
	private double interSTD;
	private double intraSTD;
//	// mean damage for mean IML
	private double mDamage_mIML; // y sub j bar
//	// high damage for mean IML
	private double hDamage_mIML; // y sub j+
//	// low damage for mean IML
	private double lDamage_mIML; // y sub j-
//	// mean damage ...
	private double mShaking; // s sub j bar
	private double imlHighInter;
	private double imlLowInter;
	private double mDamage_hInter; // s sub +t
	private double mDamage_lInter; // s sub -t
	private double imlHighIntra;
	private double imlLowIntra;
	private double mDamage_hIntra; // s sub +p
	private double mDamage_lIntra; // s sub -p
	
	private double mValue;
	private double hValue;
	private double lValue;
	
	public AssetRuptureResult(double mIML, double mLnIML, double interSTD, double intraSTD,
			double mDamage_mIML, double hDamage_mIML, double lDamage_mIML, double mShaking,
			double imlHighInter, double imlLowInter, double mDamage_hInter, double mDamage_lInter,
			double imlHighIntra, double imlLowIntra, double mDamage_hIntra,
			double mDamage_lIntra, double mValue, double hValue, double lValue) {
		super();
		this.mIML = mIML;
		this.mLnIML = mLnIML;
		this.interSTD = interSTD;
		this.intraSTD = intraSTD;
		this.mDamage_mIML = mDamage_mIML;
		this.hDamage_mIML = hDamage_mIML;
		this.lDamage_mIML = lDamage_mIML;
		this.mShaking = mShaking;
		this.imlHighInter = imlHighInter;
		this.imlLowInter = imlLowInter;
		this.mDamage_hInter = mDamage_hInter;
		this.mDamage_lInter = mDamage_lInter;
		this.imlHighIntra = imlHighIntra;
		this.imlLowIntra = imlLowIntra;
		this.mDamage_hIntra = mDamage_hIntra;
		this.mDamage_lIntra = mDamage_lIntra;
		
		this.mValue = mValue;
		this.hValue = hValue;
		this.lValue = lValue;
	}
	public double getMIML() {
		return mIML;
	}
	public double getMLnIML() {
		return mLnIML;
	}
	public double getInterSTD() {
		return interSTD;
	}
	public double getIntraSTD() {
		return intraSTD;
	}
	public double getMDamage_mIML() {
		return mDamage_mIML;
	}
	public double getHDamage_mIML() {
		return hDamage_mIML;
	}
	public double getLDamage_mIML() {
		return lDamage_mIML;
	}
	public double getMShaking() {
		return mShaking;
	}
	public double getMDamage_hInter() {
		return mDamage_hInter;
	}
	public double getMDamage_lInter() {
		return mDamage_lInter;
	}
	public double getMDamage_hIntra() {
		return mDamage_hIntra;
	}
	public double getMDamage_lIntra() {
		return mDamage_lIntra;
	}
	public double getIML_hInter() {
		return imlHighInter;
	}
	public double getIML_lInter() {
		return imlLowInter;
	}
	public double getIML_hIntra() {
		return imlHighIntra;
	}
	public double getIML_lIntra() {
		return imlLowIntra;
	}
	public double getMValue() {
		return mValue;
	}
	public double getHValue() {
		return hValue;
	}
	public double getLValue() {
		return lValue;
	}
	
	

}
