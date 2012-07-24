import org.schwa.dr.*;


@dr.Doc
public class Doc extends org.schwa.dr.Doc {
  @dr.Store public Store<Token> tokens = new Store<Token>();
  @dr.Store public Store<Chunk> chunks = new Store<Chunk>();
  @dr.Field public String filename;
}
