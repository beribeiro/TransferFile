import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class App {
	public static void main(String[] args) {
		ProcessadorArquivos process = new ProcessadorArquivos();
		List<File> listaarquivos = new ArrayList<File>(process.verificaArquivos());
		process.lerArquivo(listaarquivos);
	}
}
