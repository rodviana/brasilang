package br.edu.pucgoias.brasilang.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;

import br.edu.pucgoias.brasilang.model.error.BrasilangException;
import br.edu.pucgoias.brasilang.model.translate.OptimizationLevel;

@Service
public class CompilationService {

    /**
     * Gera assembly a partir do código C, aplicando o nível de otimização
     * solicitado.
     */
    public String compileToAssembly(String cCode, OptimizationLevel level) {
        Path workDir = Paths.get("target", "brasilang-gen");
        Path cFile = workDir.resolve("program.c");
        Path asmFile = workDir.resolve("program.s");
        try {
            Files.createDirectories(workDir);
            Files.writeString(cFile, cCode);
            ProcessBuilder pb = new ProcessBuilder("gcc", level.toFlag(), "-S",
                    cFile.getFileName().toString(), "-o", asmFile.getFileName().toString());
            pb.directory(workDir.toFile()); // usa caminhos relativos ao diretório de trabalho
            Process process = pb.start();
            String gccOutput = new String(process.getInputStream().readAllBytes())
                    + new String(process.getErrorStream().readAllBytes());
            int exit = process.waitFor();
            if (exit != 0) {
                throw new BrasilangException(
                        "Falha ao gerar assembly com GCC (código " + exit + "). Saída: " + gccOutput);
            }
            return Files.readString(asmFile);
        } catch (IOException e) {
            throw new BrasilangException("Não foi possível gerar assembly: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BrasilangException("Geração de assembly interrompida: " + e.getMessage());
        }
    }
}
