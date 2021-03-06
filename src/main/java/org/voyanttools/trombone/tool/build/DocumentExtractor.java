/*******************************************************************************
 * Trombone is a flexible text processing and analysis library used
 * primarily by Voyant Tools (voyant-tools.org).
 * 
 * Copyright (©) 2007-2012 Stéfan Sinclair & Geoffrey Rockwell
 * 
 * This file is part of Trombone.
 * 
 * Trombone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Trombone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Trombone.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.voyanttools.trombone.tool.build;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.voyanttools.trombone.input.extract.StoredDocumentSourceExtractor;
import org.voyanttools.trombone.model.DocumentMetadata;
import org.voyanttools.trombone.model.StoredDocumentSource;
import org.voyanttools.trombone.storage.Storage;
import org.voyanttools.trombone.storage.StoredDocumentSourceStorage;
import org.voyanttools.trombone.tool.utils.AbstractTool;
import org.voyanttools.trombone.util.FlexibleParameters;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * @author sgs
 *
 */
@XStreamAlias("extractedStoredDocuments")
class DocumentExtractor extends AbstractTool {

	private String storedId = null;
	
	@XStreamOmitField
	private List<StoredDocumentSource> storedDocumentSources = new ArrayList<StoredDocumentSource>();
	
	/**
	 * @param storage
	 * @param parameters
	 */
	DocumentExtractor(Storage storage, FlexibleParameters parameters) {
		super(storage, parameters);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() throws IOException {
		String sid = parameters.getParameterValue("storedId");
		List<String> ids = storage.retrieveStrings(sid);
		StoredDocumentSourceStorage storedDocumentStorage = storage.getStoredDocumentSourceStorage();
		List<StoredDocumentSource> extractableStoredDocumentSources = new ArrayList<StoredDocumentSource>();
		for (String id : ids) {
			DocumentMetadata metadata = storedDocumentStorage.getStoredDocumentSourceMetadata(id);
			StoredDocumentSource storedDocumentSource = new StoredDocumentSource(id, metadata);
			extractableStoredDocumentSources.add(storedDocumentSource);
		}
		run(extractableStoredDocumentSources);

	}
	
	void run(List<StoredDocumentSource> extractableStoredDocumentSources) throws IOException {
		
		Calendar start = Calendar.getInstance();
		log("Starting document extraction.");

		StoredDocumentSourceStorage storedDocumentStorage = storage.getStoredDocumentSourceStorage();
		StoredDocumentSourceExtractor extractor = new StoredDocumentSourceExtractor(storedDocumentStorage, parameters);
		storedDocumentSources = extractor.getExtractedStoredDocumentSources(extractableStoredDocumentSources);
		
		// sort documents if needed
		if (parameters.containsKey("sort")) {
			Collections.sort(storedDocumentSources, StoredDocumentSource.getComparator(parameters));
		}
		
		List<String> extractedIds = new ArrayList<String>();
		for (StoredDocumentSource storedDocumentSource : storedDocumentSources) {
			extractedIds.add(storedDocumentSource.getId());
		}		
		storedId = storage.storeStrings(extractedIds);
		log("Finished extraction of "+extractedIds.size()+" documents.", start);

	}

	List<StoredDocumentSource> getStoredDocumentSources() {
		return storedDocumentSources;
	}
	
	String getStoredId() {
		return storedId;
	}

}
