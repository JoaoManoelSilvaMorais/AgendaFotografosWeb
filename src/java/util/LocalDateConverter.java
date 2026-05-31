package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

/**
 * Conversor customizado para ensinar o JSF 2.2 a lidar com o LocalDate do Java 8+.
 */
// A anotação abaixo registra esse conversor no GlassFish com um "apelido"
@FacesConverter(value = "localDateConverter")
public class LocalDateConverter implements Converter {

    // Define o padrão brasileiro de datas
    private static final DateTimeFormatter FORMATADOR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * getAsObject: Pega o texto que o usuário digitou na tela (String) e converte para LocalDate (Java).
     */
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value, FORMATADOR);
        } catch (DateTimeParseException e) {
            // Se o usuário digitar "32/15/2026", barramos com uma mensagem amigável
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Data Inválida", "Use o formato dd/mm/aaaa.");
            throw new ConverterException(msg);
        }
    }

    /**
     * getAsString: Pega o LocalDate do banco de dados (Java) e converte para texto para exibir na tela (String).
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof LocalDate) {
            LocalDate data = (LocalDate) value;
            return data.format(FORMATADOR);
        }
        return value.toString();
    }
}