import { useQuery, useMutation, useQueryClient } from 'react-query';
import { fileService } from '../services/api';

export const useFiles = (page = 0, size = 20) => {
  return useQuery(
    ['files', page, size],
    () => fileService.getAllFiles(page, size),
    {
      keepPreviousData: true,
      staleTime: 5 * 60 * 1000, // 5 minutes
    }
  );
};

export const useFileMetadata = (fileId) => {
  return useQuery(
    ['file-metadata', fileId],
    () => fileService.getFileMetadata(fileId),
    {
      enabled: !!fileId,
    }
  );
};

export const useFileContent = (fileId, contentType) => {
  return useQuery(
    ['file-content', fileId],
    () => fileService.getFileContent(fileId),
    {
      enabled: !!fileId && isViewableContent(contentType),
      staleTime: 10 * 60 * 1000,
    }
  );
};

export const useFileUpload = () => {
  const queryClient = useQueryClient();
  
  return useMutation(
    ({ file, onProgress }) => fileService.uploadFile(file, onProgress),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['files']);
      },
    }
  );
};

export const useFileDelete = () => {
  const queryClient = useQueryClient();
  
  return useMutation(
    (fileId) => fileService.deleteFile(fileId),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['files']);
      },
    }
  );
};

const isViewableContent = (contentType) => {
  if (!contentType) return false;
  
  const viewableTypes = [
    'text/plain',
    'application/json',
    'image/jpeg',
    'image/png',
    'image/gif',
  ];
  
  return viewableTypes.includes(contentType);
};