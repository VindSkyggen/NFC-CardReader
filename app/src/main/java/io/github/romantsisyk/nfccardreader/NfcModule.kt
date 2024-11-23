package io.github.romantsisyk.nfccardreader

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.romantsisyk.nfccardreader.usecase.InterpretNfcDataUseCase
import io.github.romantsisyk.nfccardreader.usecase.ParseTLVUseCase
import io.github.romantsisyk.nfccardreader.usecase.ProcessNfcIntentUseCase

@Module
@InstallIn(SingletonComponent::class)
object NfcModule {

    @Provides
    fun provideInterpretNfcDataUseCase(): InterpretNfcDataUseCase {
        return InterpretNfcDataUseCase()
    }

    @Provides
    fun provideParseTLVUseCase(): ParseTLVUseCase {
        return ParseTLVUseCase()
    }

    @Provides
    fun provideProcessNfcIntentUseCase(
        parseTLVUseCase: ParseTLVUseCase,
        interpretNfcDataUseCase: InterpretNfcDataUseCase
    ): ProcessNfcIntentUseCase {
        return ProcessNfcIntentUseCase(parseTLVUseCase, interpretNfcDataUseCase)
    }
}