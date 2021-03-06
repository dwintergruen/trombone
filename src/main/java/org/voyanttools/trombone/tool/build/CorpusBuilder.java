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
import java.util.Calendar;
import java.util.List;

import org.voyanttools.trombone.input.source.InputSource;
import org.voyanttools.trombone.lucene.CorpusMapper;
import org.voyanttools.trombone.model.Corpus;
import org.voyanttools.trombone.model.CorpusMetadata;
import org.voyanttools.trombone.model.CorpusTermMinimal;
import org.voyanttools.trombone.model.CorpusTermMinimalsDB;
import org.voyanttools.trombone.model.StoredDocumentSource;
import org.voyanttools.trombone.model.TokenType;
import org.voyanttools.trombone.storage.Storage;
import org.voyanttools.trombone.tool.utils.AbstractTool;
import org.voyanttools.trombone.util.FlexibleParameters;

/**
 * @author sgs
 *
 */
public class CorpusBuilder extends AbstractTool {

	private String storedId = null;

	/**
	 * @param storage
	 * @param parameters
	 */
	public CorpusBuilder(Storage storage, FlexibleParameters parameters) {
		super(storage, parameters);
	}

	/* (non-Javadoc)
	 * @see org.voyanttools.trombone.tool.RunnableTool#run()
	 */
	@Override
	public void run() throws IOException {
		// we shouldn't get here without a storedId parameter
		String sid = parameters.getParameterValue("storedId");
		run(sid);
	}
	
	void run(String corpusId, List<StoredDocumentSource> storedDocumentSources) throws IOException {
		// we should only get here during the corpus creator sequeence – 
		// the storedDocumentSource isn't used as a parameter, but it helps enforce the sequence
		run(corpusId);
	}
	
	void run(String corpusId) throws IOException {
		// store and compute the corpus if it hasn't been stored
		if (storage.getCorpusStorage().corpusExists(corpusId)==false) {
			
			boolean verbose = parameters.getParameterBooleanValue("verbose");
						
			List<String> documentIds = storage.retrieveStrings(corpusId);
			CorpusMetadata metadata = new CorpusMetadata(corpusId);
			metadata.setDocumentIds(documentIds);
			Corpus corpus = new Corpus(storage, metadata);
			
			Calendar start = Calendar.getInstance();
			if (verbose) {log("Starting corpus terms index.");}
			CorpusMapper corpusMapper = new CorpusMapper(storage, corpus);
			// create and close to avoid concurrent requests later 
			CorpusTermMinimalsDB corpusTermMinimalsDB = CorpusTermMinimalsDB.getInstance(corpusMapper, TokenType.lexical);
			
			int totalWordTokens = 0;
			int totalWordTypes = 0;
			for (CorpusTermMinimal corpusTermMinimal : corpusTermMinimalsDB.values()) {
				totalWordTokens += corpusTermMinimal.getRawFreq();
				totalWordTypes++;
			}
			corpusTermMinimalsDB.close();
			metadata.setCreatedTime(Calendar.getInstance().getTimeInMillis());
			metadata.setTokensCount(TokenType.lexical, totalWordTokens);
			metadata.setTypesCount(TokenType.lexical, totalWordTypes);
			if (verbose) {log("Finished corpus terms index.", start);}
			
			storage.getCorpusStorage().storeCorpus(corpus, parameters);
		}
		this.storedId = corpusId;
	}

	String getStoredId() {
		return storedId;
	}

}
