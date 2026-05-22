import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../providers/app_picker_provider.dart';

class CategoryFilterBar extends ConsumerWidget {
  const CategoryFilterBar({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final categories = ref.watch(appCategoriesProvider);
    final selected = ref.watch(appCategoryFilterProvider);

    return SizedBox(
      height: 40,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 16),
        itemCount: categories.length,
        separatorBuilder: (_, __) => const SizedBox(width: 8),
        itemBuilder: (context, index) {
          final category = categories[index];
          final isSelected = category == selected;

          return FilterChip(
            label: Text(category),
            selected: isSelected,
            onSelected: (_) {
              ref.read(appCategoryFilterProvider.notifier).state =
                  category;
            },
            showCheckmark: false,
          );
        },
      ),
    );
  }
}
