package io.zksync.provider;

import org.apache.commons.lang3.tuple.Pair;

import io.zksync.domain.auth.Toggle2FA;
import io.zksync.domain.contract.ContractAddress;
import io.zksync.domain.fee.TransactionFeeBatchRequest;
import io.zksync.domain.fee.TransactionFeeDetails;
import io.zksync.domain.fee.TransactionFeeRequest;
import io.zksync.domain.operation.EthOpInfo;
import io.zksync.domain.state.AccountState;
import io.zksync.domain.token.Token;
import io.zksync.domain.token.Tokens;
import io.zksync.domain.transaction.TransactionDetails;
import io.zksync.domain.transaction.ZkSyncTransaction;
import io.zksync.signer.EthSignature;
import io.zksync.transport.ZkSyncSuccess;
import io.zksync.transport.ZkSyncTransport;
import io.zksync.transport.response.ZksAccountState;
import io.zksync.transport.response.ZksContractAddress;
import io.zksync.transport.response.ZksEthOpInfo;
import io.zksync.transport.response.ZksGetConfirmationsForEthOpAmount;
import io.zksync.transport.response.ZksSentTransaction;
import io.zksync.transport.response.ZksSentTransactionBatch;
import io.zksync.transport.response.ZksToggle2FA;
import io.zksync.transport.response.ZksTokenPrice;
import io.zksync.transport.response.ZksTokens;
import io.zksync.transport.response.ZksTransactionDetails;
import io.zksync.transport.response.ZksTransactionFeeDetails;
import io.zksync.wallet.SignedTransaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultProvider implements Provider {

    private ZkSyncTransport transport;

    private Tokens tokens;

    public DefaultProvider(ZkSyncTransport transport) {
        this.transport = transport;
        this.tokens = null;
    }

    @Override
    public AccountState getState(String accountAddress) {
        final AccountState response = transport.send("account_info", Collections.singletonList(accountAddress),
                ZksAccountState.class);

        return response;
    }

    @Override
    public TransactionFeeDetails getTransactionFee(TransactionFeeRequest feeRequest) {
        TransactionFeeDetails response = transport.send("get_tx_fee",
                Arrays.asList(feeRequest.getTransactionType().getRaw(), feeRequest.getAddress(),
                        feeRequest.getTokenIdentifier()),
                ZksTransactionFeeDetails.class);

        return response;
    }

    @Override
    public TransactionFeeDetails getTransactionFee(TransactionFeeBatchRequest feeRequest) {
        TransactionFeeDetails response = transport.send("get_txs_batch_fee_in_wei",
                Arrays.asList(feeRequest.getTransactionTypesRaw(), feeRequest.getAddresses(),
                        feeRequest.getTokenIdentifier()),
                ZksTransactionFeeDetails.class);

        return response;
    }

    @Override
    public Tokens getTokens() {
        if (this.tokens == null) {
            this.updateTokenSet();
        }

        return this.tokens;
    }

    @Override
    public BigDecimal getTokenPrice(Token token) {
        final BigDecimal response = transport.send("get_token_price", Collections.singletonList(token.getSymbol()),
                ZksTokenPrice.class);

        return response;
    }

    @Override
    public String submitTx(ZkSyncTransaction tx, EthSignature ethereumSignature, boolean fastProcessing) {
        final String responseBody = transport.send("tx_submit", Arrays.asList(tx, ethereumSignature, fastProcessing),
                ZksSentTransaction.class);

        return responseBody;
    }

    @Override
    public String submitTx(ZkSyncTransaction tx, boolean fastProcessing) {
        return submitTx(tx, null, fastProcessing);
    }

    @Override
    public String submitTx(ZkSyncTransaction tx, EthSignature... ethereumSignature) {
        final String responseBody = transport.send("tx_submit", Arrays.asList(tx, ethereumSignature),
                ZksSentTransaction.class);

        return responseBody;
    }

    @Override
    public List<String> submitTxBatch(List<Pair<ZkSyncTransaction, EthSignature>> txs, EthSignature ethereumSignature) {
        final List<String> responseBody = transport.send("submit_txs_batch", Arrays.asList(txs.stream().map(SignedTransaction::fromPair).collect(Collectors.toList()), ethereumSignature),
                ZksSentTransactionBatch.class);

        return responseBody;
    }

    @Override
    public List<String> submitTxBatch(List<Pair<ZkSyncTransaction, EthSignature>> txs) {
        return submitTxBatch(txs, null);
    }

    @Override
    public ContractAddress contractAddress() {
        final ContractAddress contractAddress = transport.send("contract_address", Collections.emptyList(),
                ZksContractAddress.class);

        return contractAddress;
    }

    @Override
    public TransactionDetails getTransactionDetails(String txHash) {
        final TransactionDetails response = transport.send("tx_info", Collections.singletonList(txHash),
                ZksTransactionDetails.class);

        return response;
    }

    @Override
    public EthOpInfo getEthOpInfo(Integer priority) {
        final EthOpInfo response = transport.send("ethop_info", Collections.singletonList(priority),
                ZksEthOpInfo.class);

        return response;
    }

    @Override
    public BigInteger getConfirmationsForEthOpAmount() {
        final BigInteger response = transport.send("get_confirmations_for_eth_op_amount", Collections.emptyList(),
                ZksGetConfirmationsForEthOpAmount.class);

        return response;
    }

    @Override
    public String getEthTransactionForWithdrawal(String zkSyncWithdrawalHash) {
        final String response = transport.send("get_eth_tx_for_withdrawal", Collections.singletonList(zkSyncWithdrawalHash),
                ZksSentTransaction.class);

        return response;
    }

    @Override
    public boolean toggle2FA(Toggle2FA toggle2fa) {
        final ZkSyncSuccess result = transport.send("toggle_2fa", Collections.singletonList(toggle2fa), ZksToggle2FA.class);

        return result.getSuccess();
    }

    public void updateTokenSet() {
        final Tokens response = transport.send("tokens", Collections.emptyList(), ZksTokens.class);
        this.tokens = response;
    }
}
