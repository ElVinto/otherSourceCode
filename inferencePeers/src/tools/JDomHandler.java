package tools;

import java.io.*;
import org.jdom2.*;
import org.jdom2.input.*;
import org.jdom2.output.*;
import java.util.List;
import java.util.Iterator;

public class JDomHandler {


	   static org.jdom2.Document document;
	   static Element racine;
	   
	 //On parse le fichier et on initialise la racine de
	   //notre arborescence
	   static void lireFichier(String fichier) throws Exception
	   {
	      SAXBuilder sxb = new SAXBuilder();
	      document = sxb.build(new File(fichier));
	      racine = document.getRootElement();
	   }

	   //On fait des modifications sur un Element
	   static void supprElement(String element)
	   {
	      //Dans un premier temps on liste tous les étudiants
	      List listEtudiant = racine.getChildren("etudiant");
	      Iterator i = listEtudiant.iterator();
	      //On parcours la liste grâce à un iterator
	      while(i.hasNext())
	      {
	         Element courant = (Element)i.next();
	         //Si l'etudiant possède l'Element en question on applique
	         //les modifications.
	         if(courant.getChild(element)!=null)
	         {
	            //On supprime l'Element en question
	            courant.removeChild(element);
	            //On renomme l'Element père sachant qu'une balise XML n'accepte
	            //ni les espaces ni les caractères spéciaux
	            //"etudiant modifié" devient "etudiant_modifie"
	            courant.setName("etudiant_modifie");
	         }
	      }
	   }

	   //On enregsitre notre nouvelle arborescence dans le fichier
	   //d'origine dans un format classique.
	   static void enregistreFichier(String fichier) throws Exception
	   {
	         XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
	         sortie.output(document, new FileOutputStream(fichier));
	   }
	   
	   
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
