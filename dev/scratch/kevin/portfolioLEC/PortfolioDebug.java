package scratch.kevin.portfolioLEC;

import java.io.File;
import java.io.IOException;

import org.opensha.sra.gui.portfolioeal.Portfolio;
import org.opensha.sra.gui.portfolioeal.PortfolioEALCalculatorController;

public class PortfolioDebug {

	public static void main(String[] args) throws IOException {
		Portfolio portfolio = Portfolio.createPortfolio(new File("/home/kevin/OpenSHA/portfolio_lec/Porter-22-May-14-CA-CAS4-90pct-Wills.txt"));
		System.out.println("Portolio has "+portfolio.getAssetList().size()+" assets");
		
		File vulnFile = new File("/home/kevin/OpenSHA/portfolio_lec/2014_05_16b_VUL06.txt");
		System.out.println("trying to load vulnerabilities from: "+vulnFile.getAbsolutePath());
		PortfolioEALCalculatorController.getVulnerabilities(vulnFile);
		System.out.println("DONE loading vulns.");
	}

}
