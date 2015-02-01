package ch.uzh.cl.slmlib.tokenize

import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations.{LemmaAnnotation, PartOfSpeechAnnotation, TokensAnnotation}
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}

import scala.collection.JavaConverters._


/**
 * Wraps <code>StanfordCoreNLP</code> pipeline library to tokenize, POS-annotate and lemmatize input strings.
 */
class LemmatizerWithPOS extends Tokenizer[(String, String)] {
  private val props = new Properties()
  props.put("annotators", "tokenize, ssplit, pos, lemma")

  // instantiation will take place after shipping to the worker thread to avoid serialization of external class
  private lazy val pipeline = new StanfordCoreNLP(props, false)

  logInfo("Lemmatizing and POS-tagging Tokenizer initialized.")

  /** Splits lines into tokens and returns sequences of tokens in (lemma,POS) form */
  override def tokenize(lines: Iterator[String]): Iterator[Seq[(String, String)]] = lines.map(line => lemmatize(line))

  private def lemmatize(line: String): Seq[(String, String)] = {
    val document = new Annotation(line)
    pipeline.annotate(document)

    val tokens = document.get[
      java.util.List[CoreLabel]
      ](classOf[TokensAnnotation]).asScala

    tokens.map(token => (token.getString(classOf[LemmaAnnotation]), token.getString(classOf[PartOfSpeechAnnotation])))
  }
}
