import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class ProcessadorArquivos {
	public List<File> verificaArquivos() {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			String filename = "config.properties";
			input = ProcessadorArquivos.class.getClassLoader().getResourceAsStream(filename);
			if (input == null) {
				System.out.println("Sorry, unable to find " + filename);
			}
			prop.load(input);
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		// Definindo a Origem de acordo com o config.properties
		String origem = prop.getProperty("origem");
		File path = new File(origem);

		// Executando os métodos lerArquivo, gravarArquivo e gerarExcel para cada
		// arquivo que termine com .txt
		List<File> a = new ArrayList<File>(Arrays.asList(path.listFiles()));
		return a;

	}

	public void lerArquivo(List<File> listaarquivos) {
		for (File file : listaarquivos) {
			if (file.getName().toLowerCase().endsWith((".txt"))) {
				Set<String> dados = new HashSet<>();
				try {
					FileReader arq = new FileReader(file.getAbsoluteFile());
					BufferedReader lerArq = new BufferedReader(arq);
					String linha = lerArq.readLine();
					while (linha != null) {
						// Aplicando os filtros, separando apenas as linhas que contenha "Insert, delete
						// ou update" e linhas que não começe com 10 espaços em branco
						if (((linha.contains("insert") || linha.contains("delete") || linha.contains("update"))
								&& !linha.substring(1, 10).trim().equals(""))) {
							dados.add(linha);
						}
						linha = lerArq.readLine();
					}
					lerArq.close();
				} catch (IOException e) {
					System.err.printf("Erro na abertura do arquivo: %s.\n", e.getMessage());
				}
				// Convertendo Set em List para realizar o ordenamento dos dados
				List<String> list = new ArrayList<String>(dados);
				orderByDateAndString(list);
				this.gerarTxt(list, file.getName());
				this.gerarExcel(list, file.getName());
			}
		}
	}

	private void orderByDateAndString(List<String> list) {
		// Alterar para Java 8
		Collections.sort(list, new Comparator<String>() {

			@Override
			// Comparador da primeira coluna (data), e da segunda coluna
			public int compare(String o1, String o2) {

				try {

					SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy hh:mm");

					Date data1 = sdf.parse(o1.substring(21, 35));

					Date data2 = sdf.parse(o2.substring(21, 35));

					String texto1 = o1.substring(0, 20);

					String texto2 = o2.substring(0, 20);

					int result = -1;

					// Compara a linha x com a linha x+1, organizando por data (Do antigo pro mais
					// novo)
					int compareDate = data1.compareTo(data2);

					// Compara a linha x com a linha x+1, organizando por ordem alfabética
					int compareString = texto1.compareTo(texto2);

					// Lógica para ordenar a segunda coluna apenas depois que a primeira coluna já
					// estiver ordenada
					if (compareString == 0) {

						if (compareDate != 0)

							result = compareDate;

					} else

						result = compareString;

					return result;

				} catch (ParseException e) {

					return 0;

				}

			}

		});
	}

	public void gerarTxt(List<String> Dados, String filename) {
		try {
			Properties prop = new Properties();
			String config = "config.properties";
			InputStream input = ProcessadorArquivos.class.getClassLoader().getResourceAsStream(config);
			prop.load(input);
			String destino = prop.getProperty("destino");
			PrintWriter writer = new PrintWriter(destino + filename, "UTF-8");
			for (String string : Dados) {
				writer.println(string);
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			System.err.printf("Erro na abertura do arquivo: %s.\n", e.getMessage());
		}

	}

	public void gerarExcel(List<String> dados, String filename) {
		try {
			Properties prop = new Properties();
			String config = "config.properties";
			InputStream input = ProcessadorArquivos.class.getClassLoader().getResourceAsStream(config);
			prop.load(input);
			String destino = prop.getProperty("destino");
			String[] columns = { "USERNAME", "TIMESTAMP", "ACTION", "SCHEMA", "OBJECT_NAME", "SQL CODE" };
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
