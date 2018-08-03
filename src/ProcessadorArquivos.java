import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class ProcessadorArquivos {
	public void verificaArquivos() {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			String filename = "config.properties";
    		input = ProcessadorArquivos.class.getClassLoader().getResourceAsStream(filename);
    		if(input==null){
    	            System.out.println("Sorry, unable to find " + filename);
    		    return;
    		}
    		prop.load(input);
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

		String origem = prop.getProperty("origem");
		String destino = prop.getProperty("destino");
		File path = new File(origem);

		for (File file : path.listFiles()) {
			Set<String> dados = this.lerArquivo(file);
			this.gravarArquivo(destino, dados, file.getName());
			this.gerarExcel(dados, destino, file.getName());
		}
	}

	public Set<String> lerArquivo(File file) {
		Set<String> dados = new HashSet<>();
		try {
			FileReader arq = new FileReader(file.getAbsoluteFile());
			BufferedReader lerArq = new BufferedReader(arq);
			String linha = lerArq.readLine();
			while (linha != null) {

				if (((linha.contains("insert") || linha.contains("delete") || linha
						.contains("update")) && !linha.substring(1, 10).trim()
						.equals(""))) {
					dados.add(linha);
				}
				linha = lerArq.readLine();
			}
			lerArq.close();
		} catch (IOException e) {
			System.err.printf("Erro na abertura do arquivo: %s.\n",
					e.getMessage());
		}
		return dados;
	}

	public void gravarArquivo(String destino, Set<String> Dados, String filename) {
		try {
			PrintWriter writer = new PrintWriter(destino + filename, "UTF-8");
			for (String string : Dados) {
				writer.println(string);
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			System.err.printf("Erro na abertura do arquivo: %s.\n",
					e.getMessage());
		}

	}

	public void gerarExcel(Set<String> dados, String destino, String filename) {
		try {
			String[] columns = { "USERNAME", "TIMESTAMP", "ACTION", "SCHEMA",
					"OBJECT_NAME", "SQL CODE" };
			// Create a Workbook
			Workbook workbook = new HSSFWorkbook(); 
			// Create a Sheet
			Sheet sheet = workbook.createSheet("Teste");

			// Create a Row
			Row headerRow = sheet.createRow(0);

			// Create cells
			for (int i = 0; i < columns.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(columns[i]);
			}

			// Create Other rows and cells with employees data
			int rowNum = 1;
			for (String valor : dados) {
				Row row = sheet.createRow(rowNum++);
				String coluna1 = valor.substring(0, 13);
				String coluna2 = valor.substring(21, 35);
				String coluna3 = valor.substring(36, 45);
				String coluna4 = valor.substring(57, 60);
				String coluna5 = valor.substring(73, 112);
				String coluna6 = valor.substring(114);
				row.createCell(0).setCellValue(coluna1);
				row.createCell(1).setCellValue(coluna2);
				row.createCell(2).setCellValue(coluna3);
				row.createCell(3).setCellValue(coluna4);
				row.createCell(4).setCellValue(coluna5);
				row.createCell(5).setCellValue(coluna6);
			}

			// Resize all columns to fit the content size
			for (int i = 0; i < columns.length; i++) {
				sheet.autoSizeColumn(i);
			}

			// Write the output to a file
			filename = filename.replaceFirst(".txt", ".xls");
			FileOutputStream fileOut;
			fileOut = new FileOutputStream(destino + filename);
			workbook.write(fileOut);
			fileOut.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}