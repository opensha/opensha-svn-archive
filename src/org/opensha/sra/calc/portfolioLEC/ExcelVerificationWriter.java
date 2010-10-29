package org.opensha.sra.calc.portfolioLEC;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.sha.earthquake.EqkRupForecastAPI;

public class ExcelVerificationWriter {
	
	private File inFile, outFile;
	
	public ExcelVerificationWriter(String inputFile, String outputFile) {
		inFile = new File(inputFile);
		outFile = new File(outputFile);
	}
	
	protected void writeResults(PortfolioRuptureResults[][] rupResults, ArbitrarilyDiscretizedFunc curve, EqkRupForecastAPI erf)
	throws FileNotFoundException, IOException {
		POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(inFile));
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		wb.setMissingCellPolicy(Row.CREATE_NULL_AS_BLANK);
		HSSFSheet sheet = wb.getSheetAt(0);
		
		ArrayList<PortfolioRuptureResults> results = new ArrayList<PortfolioRuptureResults>();
		for (int sourceID=0; sourceID<erf.getNumSources(); sourceID++) {
			for (int rupID=0; rupID<erf.getNumRuptures(sourceID); rupID++) {
				results.add(rupResults[sourceID][rupID]);
			}
		}
		int numRups = results.size();
		int numAssets = results.get(0).getAssetRupResults().size();
		
		int curRow = 4;
		// this is for the ASSETS part
		for (int assetNum=0; assetNum<numAssets; assetNum++) {
			HSSFRow mLnRow = sheet.getRow(curRow++);
			HSSFRow interSTDRow = sheet.getRow(curRow++);
			HSSFRow intraSTDRow = sheet.getRow(curRow++);
			HSSFRow mIMLRow = sheet.getRow(curRow++);
			curRow++; // medIML
			HSSFRow mDamage_mIMLRow = sheet.getRow(curRow++);
			curRow++; // deltaj_mIML
			curRow++; // betaj_mIML
			curRow++; // medDamage_mIML
			HSSFRow hDamage_mIMLRow = sheet.getRow(curRow++);
			HSSFRow lDamage_mIMLRow = sheet.getRow(curRow++);
			HSSFRow imlHighInterRow = sheet.getRow(curRow++);
			HSSFRow imlLowInterRow = sheet.getRow(curRow++);
			HSSFRow imlHighIntraRow = sheet.getRow(curRow++);
			HSSFRow imlLowIntraRow = sheet.getRow(curRow++);
			HSSFRow mDamage_hInterRow = sheet.getRow(curRow++);
			HSSFRow mDamage_lInterRow = sheet.getRow(curRow++);
			HSSFRow mDamage_hIntraRow = sheet.getRow(curRow++);
			HSSFRow mDamage_lIntraRow = sheet.getRow(curRow++);
			curRow++; // separator row
			
			for (int rupID=0; rupID<numRups; rupID++) {
				PortfolioRuptureResults rupResult = results.get(rupID);
				int cellNum = 1+rupID;
				AssetRuptureResult res = rupResult.getAssetRupResults().get(assetNum);
				
				mLnRow.getCell(cellNum).setCellValue(res.getMLnIML());
				interSTDRow.getCell(cellNum).setCellValue(res.getInterSTD());
				intraSTDRow.getCell(cellNum).setCellValue(res.getIntraSTD());
				mIMLRow.getCell(cellNum).setCellValue(res.getMIML());
				mDamage_mIMLRow.getCell(cellNum).setCellValue(res.getMDamage_mIML());
				hDamage_mIMLRow.getCell(cellNum).setCellValue(res.getHDamage_mIML());
				lDamage_mIMLRow.getCell(cellNum).setCellValue(res.getLDamage_mIML());
				imlHighInterRow.getCell(cellNum).setCellValue(res.getIML_hInter());
				imlLowInterRow.getCell(cellNum).setCellValue(res.getIML_lInter());
				System.out.println(imlHighIntraRow.getCell(0).getStringCellValue());
				System.out.println(imlHighIntraRow.getCell(cellNum).getStringCellValue());
				imlHighIntraRow.getCell(cellNum).setCellValue(res.getIML_hIntra());
				imlLowIntraRow.getCell(cellNum).setCellValue(res.getIML_lIntra());
				mDamage_hInterRow.getCell(cellNum).setCellValue(res.getMDamage_hInter());
				mDamage_lInterRow.getCell(cellNum).setCellValue(res.getMDamage_lInter());
				mDamage_hIntraRow.getCell(cellNum).setCellValue(res.getMDamage_hIntra());
				mDamage_lIntraRow.getCell(cellNum).setCellValue(res.getMDamage_lIntra());
			}
		}
		int numLs = results.get(0).getL().length;
		
		// now results for each asset
		for (int assetNum=0; assetNum<numAssets; assetNum++) {
			HSSFRow mValueRow = sheet.getRow(curRow++);
			HSSFRow hValueRow = sheet.getRow(curRow++);
			HSSFRow lValueRow = sheet.getRow(curRow++);
			
			curRow += numLs*2; // skip the Ls
			curRow++; // other data
			curRow++; // other data
			curRow++; // separator row
			
			for (int rupID=0; rupID<numRups; rupID++) {
				PortfolioRuptureResults rupResult = results.get(rupID);
				int cellNum = 1+rupID;
				AssetRuptureResult res = rupResult.getAssetRupResults().get(assetNum);
				
				mValueRow.getCell(cellNum).setCellValue(res.getMValue());
				hValueRow.getCell(cellNum).setCellValue(res.getHValue());
				lValueRow.getCell(cellNum).setCellValue(res.getLValue());
			}
		}
		
		// portfolio section
		HSSFRow[] lRows = new HSSFRow[numLs];
		HSSFRow[] lSquaredRows = new HSSFRow[numLs];
		
		for (int lInd=0; lInd<numLs; lInd++) {
			lRows[lInd] = sheet.getRow(curRow++);
			lSquaredRows[lInd] = sheet.getRow(curRow++);
		}
		for (int rupID=0; rupID<numRups; rupID++) {
			PortfolioRuptureResults rupResult = results.get(rupID);
			int cellNum = 1+rupID;
			
			for (int lInd=0; lInd<numLs; lInd++) {
				lRows[lInd].getCell(cellNum).setCellValue(rupResult.getL()[lInd]);
				lSquaredRows[lInd].getCell(cellNum).setCellValue(rupResult.getLSquared()[lInd]);
			}
		}
		curRow++; // other data
		curRow++; // other data
		curRow++; // separator row
		
		// portfolio scenario loss section
		HSSFRow w0Row = sheet.getRow(curRow++);
		HSSFRow wiRow = sheet.getRow(curRow++);
		HSSFRow e_LgivenSRow = sheet.getRow(curRow++);
		HSSFRow e_LSuqaredGivenSRow = sheet.getRow(curRow++);
		HSSFRow varLgivenRow = sheet.getRow(curRow++);
		HSSFRow deltaSquaredSubLgivenSRow = sheet.getRow(curRow++);
		HSSFRow thetaSubLgivenSRow = sheet.getRow(curRow++);
		HSSFRow betaSubLgivenSRow = sheet.getRow(curRow++);
		curRow++; // separator row
		
		for (int rupID=0; rupID<numRups; rupID++) {
			PortfolioRuptureResults rupResult = results.get(rupID);
			int cellNum = 1+rupID;
			
			w0Row.getCell(cellNum).setCellValue(rupResult.getW0());
			wiRow.getCell(cellNum).setCellValue(rupResult.getWi());
			e_LgivenSRow.getCell(cellNum).setCellValue(rupResult.getE_LgivenS());
			e_LSuqaredGivenSRow.getCell(cellNum).setCellValue(rupResult.getE_LSuqaredGivenS());
			varLgivenRow.getCell(cellNum).setCellValue(rupResult.getVarLgivenS());
			deltaSquaredSubLgivenSRow.getCell(cellNum).setCellValue(rupResult.getDeltaSquaredSubLgivenS());
			thetaSubLgivenSRow.getCell(cellNum).setCellValue(rupResult.getThetaSubLgivenS());
			betaSubLgivenSRow.getCell(cellNum).setCellValue(rupResult.getBetaSubLgivenS());
		}
		
		// portfolio scenario LEC section
		int numCurvePts = results.get(0).getExceedanceProbs().getNum();
		HSSFRow[] exceedanceCurveRows = new HSSFRow[numCurvePts];
		for (int curveI=0; curveI<numCurvePts; curveI++) {
			exceedanceCurveRows[curveI] = sheet.getRow(curRow++);
		}
		curRow++; // separator row
		
		for (int rupID=0; rupID<numRups; rupID++) {
			PortfolioRuptureResults rupResult = results.get(rupID);
			int cellNum = 1+rupID;
			
			ArbitrarilyDiscretizedFunc exceedProbs = rupResult.getExceedanceProbs();
			
			for (int curveI=0; curveI<numCurvePts; curveI++) {
				exceedanceCurveRows[curveI].getCell(cellNum).setCellValue(exceedProbs.getY(curveI));
			}
		}
		
		// skip prob LEC curves
		curRow += numCurvePts;
		curRow++; // separator row
		
		// FINAL CURVE
		for (int curveI=0; curveI<numCurvePts; curveI++) {
			sheet.getRow(curRow++).getCell(1).setCellValue(curve.getY(curveI));
		}
		
		// now write it out
		FileOutputStream fileOut = new FileOutputStream(outFile);
		wb.write(fileOut);
		fileOut.close();
	}

}
