package com.ztbsuper.dingtalk;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.ztbsuper.Messages;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import ren.wizard.dingtalkclient.DingTalkClient;
import ren.wizard.dingtalkclient.message.DingMessage;
import ren.wizard.dingtalkclient.message.MarkdownMessage;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * @author uyangjie
 */
public class DingMdTalkNotifier extends Notifier implements SimpleBuildStep {

    private String accessToken;
    private String title;
    private String content;
    private String atMobiles;
    private Boolean isAtAll;

    @DataBoundConstructor
    public DingMdTalkNotifier(String accessToken, String title, String content, String atMobiles, Boolean isAtAll) {
        this.accessToken = accessToken;
        this.title = title;
        this.content = content;
        this.atMobiles = atMobiles;
        this.isAtAll = isAtAll == null ? false : isAtAll;
    }

    public String getAccessToken() {
        return accessToken;
    }

    @DataBoundSetter
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTitle() {
        return title;
    }

    @DataBoundSetter
    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public String getContent() {
        return content;
    }

    @DataBoundSetter
    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    public String getAtMobiles() {
        return atMobiles;
    }

    @DataBoundSetter
    public void setAtMobiles(String atMobiles) {
        this.atMobiles = atMobiles == null ? null : atMobiles.trim();
    }

    public Boolean getIsAtAll() {
        return isAtAll;
    }

    @DataBoundSetter
    public void setIsAtAll(Boolean isAtAll) {
        this.isAtAll = isAtAll;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath filePath, @Nonnull Launcher launcher, @Nonnull TaskListener taskListener) throws InterruptedException, IOException {
        if (!StringUtils.isBlank(content)) {
            List<String> list = Lists.newArrayList();
            if(StringUtils.isNotBlank(atMobiles)){
                Iterable<String> split = Splitter.on(",").split(atMobiles);
                list = IteratorUtils.toList(split.iterator());
            }
            sendMessage(MarkdownMessage.builder()
                                       .title(title)
                                       .item(content)
                                       .atMobiles(list)
                                       .isAtAll(isAtAll)
                                       .build());
        }
    }

    private void sendMessage(DingMessage message) {
        DingTalkClient dingTalkClient = DingTalkClient.getInstance();
        try {
            dingTalkClient.sendMessage(accessToken, message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Symbol("dingMdTalk")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public FormValidation doCheck(@QueryParameter String accessToken, @QueryParameter String notifyPeople) {
            if (StringUtils.isBlank(accessToken)) {
                return FormValidation.error(Messages.DingTalkNotifier_DescriptorImpl_AccessTokenIsNecessary());
            }
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.DingTalkNotifier_DescriptorImpl_DisplayName();
        }
    }
}
