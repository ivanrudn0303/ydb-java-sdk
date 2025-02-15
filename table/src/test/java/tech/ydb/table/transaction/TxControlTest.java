package tech.ydb.table.transaction;

import tech.ydb.proto.table.YdbTable.OnlineModeSettings;
import tech.ydb.proto.table.YdbTable.SerializableModeSettings;
import tech.ydb.proto.table.YdbTable.StaleModeSettings;
import tech.ydb.proto.table.YdbTable.TransactionControl;
import tech.ydb.proto.table.YdbTable.TransactionSettings;

import com.google.common.truth.Truth;
import com.google.common.truth.extensions.proto.ProtoTruth;
import org.junit.Test;

/**
 * @author Sergey Polovko
 */
public class TxControlTest {

    @Test
    public void id() {
        TransactionControl.Builder txPb = TransactionControl.newBuilder()
            .setCommitTx(true)
            .setTxId("some-id");

        TxControl<?> tx = TxControl.id("some-id");
        Truth.assertThat(tx.isCommitTx()).isTrue();
        ProtoTruth.assertThat(tx.toPb()).isEqualTo(txPb.build());

        TxControl<?> txNoCommit = tx.setCommitTx(false);
        Truth.assertThat(txNoCommit.isCommitTx()).isFalse();
        ProtoTruth.assertThat(txNoCommit.toPb())
            .isEqualTo(txPb.setCommitTx(false).build());

        Truth.assertThat(tx.setCommitTx(true)).isSameInstanceAs(tx);
        Truth.assertThat(txNoCommit.setCommitTx(false)).isSameInstanceAs(txNoCommit);
    }

    @Test
    public void serializableRw() {
        TransactionControl.Builder txPb = TransactionControl.newBuilder()
            .setCommitTx(true)
            .setBeginTx(TransactionSettings.newBuilder()
                .setSerializableReadWrite(SerializableModeSettings.getDefaultInstance())
                .build());

        TxControl<?> tx = TxControl.serializableRw();
        Truth.assertThat(tx.isCommitTx()).isTrue();
        ProtoTruth.assertThat(tx.toPb()).isEqualTo(txPb.build());

        TxControl<?> txNoCommit = tx.setCommitTx(false);
        Truth.assertThat(txNoCommit.isCommitTx()).isFalse();
        ProtoTruth.assertThat(txNoCommit.toPb())
            .isEqualTo(txPb.setCommitTx(false).build());

        Truth.assertThat(tx.setCommitTx(true)).isSameInstanceAs(tx);
        Truth.assertThat(tx.setCommitTx(false)).isSameInstanceAs(txNoCommit);
        Truth.assertThat(txNoCommit.setCommitTx(true)).isSameInstanceAs(tx);
        Truth.assertThat(txNoCommit.setCommitTx(false)).isSameInstanceAs(txNoCommit);
    }

    @Test
    public void staleRo() {
        TransactionControl.Builder txPb = TransactionControl.newBuilder()
            .setCommitTx(true)
            .setBeginTx(TransactionSettings.newBuilder()
                .setStaleReadOnly(StaleModeSettings.getDefaultInstance())
                .build());

        TxControl<?> tx = TxControl.staleRo();
        Truth.assertThat(tx.isCommitTx()).isTrue();
        ProtoTruth.assertThat(tx.toPb()).isEqualTo(txPb.build());

        TxControl<?> txNoCommit = tx.setCommitTx(false);
        Truth.assertThat(txNoCommit.isCommitTx()).isFalse();
        ProtoTruth.assertThat(txNoCommit.toPb())
            .isEqualTo(txPb.setCommitTx(false).build());

        Truth.assertThat(tx.setCommitTx(true)).isSameInstanceAs(tx);
        Truth.assertThat(tx.setCommitTx(false)).isSameInstanceAs(txNoCommit);
        Truth.assertThat(txNoCommit.setCommitTx(true)).isSameInstanceAs(tx);
        Truth.assertThat(txNoCommit.setCommitTx(false)).isSameInstanceAs(txNoCommit);
    }

    @Test
    public void onlineRo() {
        TransactionControl.Builder txPb = TransactionControl.newBuilder()
            .setCommitTx(true)
            .setBeginTx(TransactionSettings.newBuilder()
                .setOnlineReadOnly(OnlineModeSettings.getDefaultInstance())
                .build());

        TxControl.TxOnlineRo tx = TxControl.onlineRo();
        Truth.assertThat(tx.isAllowInconsistentReads()).isFalse();
        Truth.assertThat(tx.isCommitTx()).isTrue();
        ProtoTruth.assertThat(tx.toPb()).isEqualTo(txPb.build());

        TxControl.TxOnlineRo txNoCommit = tx.setCommitTx(false);
        Truth.assertThat(tx.isAllowInconsistentReads()).isFalse();
        Truth.assertThat(txNoCommit.isCommitTx()).isFalse();
        ProtoTruth.assertThat(txNoCommit.toPb())
            .isEqualTo(txPb.setCommitTx(false).build());

        Truth.assertThat(tx.setCommitTx(true)).isSameInstanceAs(tx);
        Truth.assertThat(tx.setCommitTx(false)).isSameInstanceAs(txNoCommit);
        Truth.assertThat(txNoCommit.setCommitTx(true)).isSameInstanceAs(tx);
        Truth.assertThat(txNoCommit.setCommitTx(false)).isSameInstanceAs(txNoCommit);

        TxControl.TxOnlineRo txInconsistentReads = tx.setAllowInconsistentReads(true);
        Truth.assertThat(txInconsistentReads.isAllowInconsistentReads()).isTrue();
        ProtoTruth.assertThat(txInconsistentReads.toPb())
            .isEqualTo(TransactionControl.newBuilder()
                .setCommitTx(true)
                .setBeginTx(TransactionSettings.newBuilder()
                    .setOnlineReadOnly(OnlineModeSettings.newBuilder().setAllowInconsistentReads(true)))
                .build());

        TxControl.TxOnlineRo txInconsistentReadsNoCommit = txNoCommit.setAllowInconsistentReads(true);
        Truth.assertThat(txInconsistentReads.isAllowInconsistentReads()).isTrue();
        ProtoTruth.assertThat(txInconsistentReadsNoCommit.toPb())
            .isEqualTo(TransactionControl.newBuilder()
                .setCommitTx(false)
                .setBeginTx(TransactionSettings.newBuilder()
                    .setOnlineReadOnly(OnlineModeSettings.newBuilder().setAllowInconsistentReads(true)))
                .build());

        Truth.assertThat(tx.setAllowInconsistentReads(false)).isSameInstanceAs(tx);
        Truth.assertThat(txNoCommit.setAllowInconsistentReads(false)).isSameInstanceAs(txNoCommit);
        Truth.assertThat(txInconsistentReads.setAllowInconsistentReads(true)).isSameInstanceAs(txInconsistentReads);
        Truth.assertThat(txInconsistentReadsNoCommit.setAllowInconsistentReads(true)).isSameInstanceAs(txInconsistentReadsNoCommit);
    }
}
