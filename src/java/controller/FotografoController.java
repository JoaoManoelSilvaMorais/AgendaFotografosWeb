package controller;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext; // Importação correta do contexto do JSF

import model.Fotografo;
import persistencia.FotografoDAO;

/**
 * Controller responsável por ligar a tela de cadastro de fotógrafos ao backend.
 * Implementa Serializable pois o escopo ViewScoped exige que os dados possam ser 
 * serializados na memória do servidor GlassFish.
 */
@ManagedBean
@ViewScoped
public class FotografoController implements Serializable {

    // O objeto que vai receber os dados digitados nos campos de texto da tela
    private Fotografo fotografo;
    
    // A lista que vai preencher a tabela de dados (dataTable) na tela
    private List<Fotografo> listaFotografos;
    
    private FotografoDAO dao;

    /**
     * Construtor: Executado assim que o usuário acessa a tela de fotógrafos.
     */
    public FotografoController() {
        this.dao = new FotografoDAO();
        // Inicializamos o objeto vazio para que a tela não dê erro ao tentar carregar os campos
        this.fotografo = new Fotografo(); 
        this.fotografo.setDataContratacao(LocalDate.now()); // Sugere a data de hoje por padrão
    }

    /**
     * Método chamado pelo botão "Salvar" da tela web.
     */
    public void salvar() {
        boolean sucesso = dao.salvar(fotografo);
        
        // AQUI ESTÁ O SEGREDO: Usamos getCurrentInstance() e não getContext()
        FacesContext context = FacesContext.getCurrentInstance();
        
        if (sucesso) {
            // Envia uma mensagem verde (SEVERITY_INFO) para a tela
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Sucesso", "Fotógrafo cadastrado com sucesso!"));
            
            // Limpa o formulário instanciando um novo objeto
            this.fotografo = new Fotografo();
            this.fotografo.setDataContratacao(LocalDate.now());
            
            // Força a atualização da lista para que o novo cadastro já apareça na tabela abaixo
            this.listaFotografos = null; 
            
        } else {
            // Envia uma mensagem vermelha (SEVERITY_ERROR) para a tela
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Erro", "Ocorreu um problema ao cadastrar. Verifique se o CPF já existe."));
        }
    }
    
    /**
     * Método acionado pelo botão de exclusão da tabela na tela web.
     * @param f O objeto Fotografo selecionado na linha da tabela.
     */
    public void excluir(Fotografo f) {
        // Envia o ID do objeto para o método de exclusão do DAO
        boolean sucesso = dao.excluir(f.getId());
        
        FacesContext context = FacesContext.getCurrentInstance();
        
        if (sucesso) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Sucesso", "Fotógrafo removido com sucesso!"));
            
            // Força a recarga da tabela limpando a lista na memória (Lazy Loading)
            this.listaFotografos = null; 
        } else {
            // Caso o DAO retorne false (ex: violação de integridade por estar escalado)
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Erro ao excluir", "Não é possível remover este fotógrafo pois ele está vinculado a um evento."));
        }
    }

    // --- GETTERS E SETTERS ---
    // O JSF exige os getters e setters para conseguir ler e escrever na tela!

    public Fotografo getFotografo() {
        return fotografo;
    }

    public void setFotografo(Fotografo fotografo) {
        this.fotografo = fotografo;
    }

    /**
     * Método chamado pela tabela da tela web (dataTable) para listar os registros.
     * Usa o conceito de "Lazy Loading" básico: só vai ao banco buscar se a lista estiver nula.
     */
    public List<Fotografo> getListaFotografos() {
        if (listaFotografos == null) {
            listaFotografos = dao.listarTodos();
        }
        return listaFotografos;
    }
}