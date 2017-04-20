package co.edu.usa.adf.Fix_width_framework;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

public class FrameWidth<T> {

	private String nombreClase;
	private String rutaArchivo;
	private String rutaGuardar;
	private HashMap <Integer, AtribDat> datosAtributos = new HashMap<Integer, AtribDat>();
	private ArrayList<T> datos = new ArrayList<T>();
	
	public FrameWidth(String rutaDescriptor) throws Exception{
		System.out.println("Leyendo descriptor ----------------------------------->");
		BufferedReader leer= new BufferedReader(new FileReader(rutaDescriptor));
			this.nombreClase=leer.readLine();
			this.rutaArchivo=leer.readLine();
			this.rutaGuardar=leer.readLine();
			System.out.println("Nombre Clase: "+nombreClase+" \nRuta Archivo: "+rutaArchivo+" \nRuta Guardar: "+rutaGuardar);
		leer.close();
		System.out.println("Descriptor Leido! ------------------------------------>\n");
		
		System.out.println("Leyendo Atributos de la clase ------------------------>");
		Class<?> miClase = Class.forName(nombreClase);
		Field[] variables = miClase.getDeclaredFields();
		for (Field variable : variables){	
			FixedWidthField anotacionObtenida = variable.getAnnotation(FixedWidthField.class);
			if (anotacionObtenida != null) {
				String nombre = (variable.getName().charAt(0)+"").toUpperCase()+(variable.getName().substring(1));
				String tipo = variable.getType().getSimpleName();
				int posicion = anotacionObtenida.posicion();
				int ancho = anotacionObtenida.width();
				datosAtributos.put(posicion, new AtribDat(nombre, tipo, ancho));
				System.out.println("Posicion: "+posicion+" Ancho: "+ancho+" tipo: "+tipo+" nombre: "+nombre);
			}
		}
		System.out.println("Atributos leidos! ------------------------------------>\n");
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<T> leerArchivo() throws Exception{
		System.out.println("Leyendo datos----------------------------------------->");
		BufferedReader leer = new BufferedReader(new FileReader(rutaArchivo));
		Class<?> cls = Class.forName(nombreClase);
		String cadena="";
		while((cadena=leer.readLine())!=null){
			if(cadena.length()>0){
				Object inst = cls.newInstance();
				for (int i = 0; i < datosAtributos.size(); i++) {
					String info="";	
					if(datosAtributos.get(i).getAncho()<=cadena.length()){
						info = cadena.substring(0, datosAtributos.get(i).getAncho()).trim();
						cadena= cadena.substring(datosAtributos.get(i).getAncho());
					}else info = cadena.substring(0, cadena.length()).trim();
					guardarDato(i, info, inst, cls);
				}
				datos.add(validarObjeto((T)inst));
				System.out.println("Guardado dato --> "+inst);
			}
		}
		//System.out.println(datos);
		leer.close();
		System.out.println("datos leidos!----------------------------------------->\n");
		return datos;
	}
	
	private void guardarDato(int indexAtrib, String dato, Object inst, Class<?> clase) throws Exception{
		Class<?> cls = null;
		Object d = null;
		switch (datosAtributos.get(indexAtrib).getTipo().toLowerCase()) {
			case "boolean":	cls = boolean.class; d = Boolean.parseBoolean(dato); break;
			case "int":	cls = int.class; d = Integer.parseInt(dato); break;
			case "integer": cls = java.lang.Integer.class; d = Integer.parseInt(dato); break;
			case "long": cls = long.class; d = Long.parseLong(dato); break;
			case "char": cls = char.class; d = (dato+"").charAt(0); break;
			case "string": cls = java.lang.String.class; d = (dato+""); break;
			case "double": cls = double.class; d = Double.parseDouble(dato); break;
			case "float": cls = float.class; d = Float.parseFloat(dato); break;
			case "date": SimpleDateFormat formato=null; formato = new SimpleDateFormat("yyyy/MM/dd");
						 cls = Date.class; d = formato.parse(dato); break;
			default: System.out.println("No se encontro el tipo de dato!---------->"); break;
		}
		Method m = clase.getMethod("set"+datosAtributos.get(indexAtrib).getNombre(), cls);
		m.invoke(inst, d);
	}
	
	public void guardarDatos(boolean reOrdenar) throws Exception{
		System.out.println("Guardando en persistencia----------------------------->");
		if(reOrdenar) reordenarArray();
		Class<?> cls = Class.forName(nombreClase);
		BufferedWriter escribir = new BufferedWriter(new FileWriter(rutaGuardar));
			for (int i = 0; i < datos.size(); i++) {
				String info="";
				for (int j = 0; j < datosAtributos.size(); j++) {
					String getis = "get";
					if(datosAtributos.get(j).getTipo().equals("boolean")) getis = "is";
					Method m = cls.getMethod(getis+datosAtributos.get(j).getNombre());
					if(!datosAtributos.get(j).getTipo().equalsIgnoreCase("date")){
						info+=String.format("%1$-"+datosAtributos.get(j).getAncho()+"s", m.invoke(datos.get(i)));
					}else{
						SimpleDateFormat formato = null;
						formato = new SimpleDateFormat("yyyy/MM/dd");
						info+=String.format("%1$-"+datosAtributos.get(j).getAncho()+"s", formato.format(m.invoke(datos.get(i))));
					}
				}
				System.out.println("Guardando en persistencia --> "+info);
				escribir.write(info);
				escribir.newLine();
			}
		escribir.close();
		System.out.println("Datos Guardados en persistencia!---------------------->\n");
	}
	
	private void reordenarArray(){
		Random rndm = new Random();
        rndm.setSeed(1000);
        Collections.shuffle(datos, rndm);
	}
	
	public ArrayList<T> getDatos() {
		return datos;
	}

	public void setDatos(ArrayList<T> datos) throws Exception {
		for (int i = 0; i < datos.size(); i++) {
			datos.add((T)validarObjeto(datos.remove(i)));
		}
		this.datos = datos;
	}
	
	public T getDato(int i) {
		return datos.get(i);
	}

	public void setDato(int i, T dato) throws Exception {
		this.datos.set(i, validarObjeto(dato));
	}
	
	public void add(T dato) throws Exception {
		this.datos.add(validarObjeto(dato));
	}
	
	private T validarObjeto(T dato) throws Exception{
		Class<?> cls = Class.forName(nombreClase);
		for (int i = 0; i < datosAtributos.size(); i++) {
			String getis = "get";
			if(datosAtributos.get(i).getTipo().equals("boolean")) getis = "is";
			Method m = cls.getMethod(getis+datosAtributos.get(i).getNombre());
			String info = m.invoke(dato)+"";
			int n = info.length();
			if(n>datosAtributos.get(i).getAncho() && !datosAtributos.get(i).getTipo().equalsIgnoreCase("date")){
				info=info.substring(0, datosAtributos.get(i).getAncho());
				guardarDato(i, info, dato, cls);
			}
		}
		return dato;
	}
	
	public void remove(int i){
		datos.remove(i);
	}

	public String getRutaArchivo() {
		return rutaArchivo;
	}

	public void setRutaArchivo(String rutaArchivo) {
		this.rutaArchivo = rutaArchivo;
	}

	public String getRutaGuardar() {
		return rutaGuardar;
	}

	public void setRutaGuardar(String rutaGuardar) {
		this.rutaGuardar = rutaGuardar;
	}
	
	public int size(){
		return datos.size();
	}
	
	public class AtribDat {
		private String nombre;
		private String tipo;
		private int ancho;
		
		public AtribDat(String nombre, String tipo, int ancho) {
			this.nombre = nombre;
			this.tipo = tipo;
			this.ancho = ancho;
		}
		
		public String getNombre() {
			return nombre;
		}

		public void setNombre(String nombre) {
			this.nombre = nombre;
		}

		public String getTipo() {
			return tipo;
		}
		
		public void setTipo(String tipo) {
			this.tipo = tipo;
		}
		
		public int getAncho() {
			return ancho;
		}
		
		public void setAncho(int ancho) {
			this.ancho = ancho;
		}

		@Override
		public String toString() {
			return "AtribDat [nombre=" + nombre + ", tipo=" + tipo + ", ancho=" + ancho + "]";
		}
	}
}
