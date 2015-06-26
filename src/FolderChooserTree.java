import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
public class FolderChooserTree extends JPanel {

	public ArrayList<TreePath> folders;

	/** File-system tree. Built Lazily */
	private JTree tree;
	private DefaultTreeModel treeModel;

	private FileSystemView fileSystemView;

	public FolderChooserTree() {
		folders = new ArrayList<TreePath>();
		fileSystemView = FileSystemView.getFileSystemView();
		// the File tree
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		treeModel = new DefaultTreeModel(root);

		TreeSelectionListener treeSelectionListener = new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent tse) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tse
						.getPath().getLastPathComponent();
				getList(node, (File) node.getUserObject());

				// Lazily updates currently selected nodes
				TreePath[] tp = tree.getSelectionPaths();
				folders.clear();
				for (TreePath t : tp) {
					folders.add(t);
				}
			}
		};

		// show the file system roots.
		File[] roots = fileSystemView.getRoots();

		for (File fileSystemRoot : roots) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(
					fileSystemRoot);
			System.out.println(fileSystemRoot.getAbsolutePath());
			root.add(node);
			File[] files = fileSystemView.getFiles(fileSystemRoot, true);
			for (File file : files) {
				if (file.isDirectory()) {
					node.add(new DefaultMutableTreeNode(file));
				}
			}
			//
		}

		tree = new JTree(treeModel);
		tree.setRootVisible(false);
		tree.addTreeSelectionListener(treeSelectionListener);
		tree.setCellRenderer(new FileTreeCellRenderer());
		tree.expandRow(0);
		JScrollPane treeScroll = new JScrollPane(tree);

		// as per trashgod tip
		tree.setVisibleRowCount(15);
		add(treeScroll);
	}

	// Displays a node

	public void getList(DefaultMutableTreeNode node, File f) {
		
		if (f.isDirectory()) {
			File fList[] = f.listFiles();

			for (int i = 0; i < fList.length; i++)
				if (fList[i].isDirectory()) {
					node.add(new DefaultMutableTreeNode(fList[i]));
				}
			// getList(child, fList[i]);
		}
	}

	/** A TreeCellRenderer for a File. */
	class FileTreeCellRenderer extends DefaultTreeCellRenderer {

		private FileSystemView fileSystemView;

		private JLabel label;
		private JCheckBox box;

		FileTreeCellRenderer() {
			label = new JLabel();
			box = new JCheckBox();
			label.setOpaque(true);
			fileSystemView = FileSystemView.getFileSystemView();
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean selected, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			File file = (File) node.getUserObject();
			label.setIcon(fileSystemView.getSystemIcon(file));
			label.setText(fileSystemView.getSystemDisplayName(file));
			label.setToolTipText(file.getAbsolutePath());

			if (selected) {
				label.setBackground(backgroundSelectionColor);
				label.setForeground(textSelectionColor);
				box.setSelected(true);
			} else {
				label.setBackground(backgroundNonSelectionColor);
				label.setForeground(textNonSelectionColor);
				box.setSelected(false);
			}
			
			JPanel jp = new JPanel(new GridBagLayout());
			jp.setSize(jp.getWidth(), jp.getHeight()-8);
			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(0, 0, 0, 0);
			//jp.setPreferredSize(new Dimension(label.getSize().width+12,12));
			jp.add(box,c);
			jp.add(label,c);
			return jp;
		}
	}
}
