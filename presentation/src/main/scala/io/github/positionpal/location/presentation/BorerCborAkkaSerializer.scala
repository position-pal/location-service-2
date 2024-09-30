package io.github.positionpal.location.presentation

import scala.reflect.ClassTag

import akka.serialization.Serializer
import io.bullet.borer.{Cbor, Codec, Decoder, Encoder}

/** A custom Akka serializer that uses <a href="https://sirthias.github.io/borer/index.html">Borer</a>
  * CBOR for encoding and decoding messages.
  *
  * @see <a href="https://github.com/scala-steward/csw/blob/e3f1970b1c374693be0c4bbec068183e3395010f/csw-commons/src/main/scala/csw/commons/CborAkkaSerializer.scala">original source</a>.
  */
trait BorerCborAkkaSerializer extends Serializer:

  private var registrations: List[(Class[?], Codec[?])] = Nil

  protected def register[T: Encoder: Decoder: ClassTag](): Unit =
    registrations ::= scala.reflect.classTag[T].runtimeClass -> Codec.of[T]

  override def includeManifest: Boolean = true

  override def toBinary(o: AnyRef): Array[Byte] =
    val codec = getCodec(o.getClass, "encoding")
    val encoder = codec.encoder.asInstanceOf[Encoder[AnyRef]]
    Cbor.encode(o)(using encoder).toByteArray

  override def fromBinary(bytes: Array[Byte], manifest: Option[Class[?]]): AnyRef =
    val codec = getCodec(manifest.get, "decoding")
    val decoder = codec.decoder.asInstanceOf[Decoder[AnyRef]]
    Cbor.decode(bytes).to[AnyRef](using decoder).value

  private def getCodec(classValue: Class[?], action: String): Codec[?] =
    registrations.collectFirst:
      case (clazz, codec) if clazz.isAssignableFrom(classValue) => codec
    .getOrElse(throw new RuntimeException(s"$action of $classValue is not configured"))
